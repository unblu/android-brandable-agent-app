package com.unblu.brandeableagentapp.login.sso

import android.content.Context
import android.util.Log
import androidx.work.*
import com.unblu.brandeableagentapp.AgentApplication
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.sso.TokenRefreshWorker.Companion.TOKEN_REQUEST
import com.unblu.brandeableagentapp.model.AuthenticationType
import net.openid.appauth.AuthState
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.TokenRequest
import java.util.concurrent.TimeUnit

/**
 *  This class can be deleted in case the selected [AuthenticationType] is not [AuthenticationType.OAuth].
 *
 *  This worker is responsible for mantaining the refresh cycle for the authentication worker.
 *
 * @property workerParams WorkerParameters
 * @constructor
 */
class TokenRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    companion object {
        const val TAG = "TokenRefreshWorker"
        const val TOKEN_REQUEST = "token_request"
    }

    private var workerParams: WorkerParameters

    init {
        this.workerParams = workerParams
    }

    override fun doWork(): Result {
        val application = applicationContext as AgentApplication
        // Retrieve the stored AuthState
        val authState: AuthState = workerParams.inputData.getString(AUTH_STATE)
            ?.let { data -> AuthState.jsonDeserialize(data) }
            ?: kotlin.run { return Result.failure() }
        // Retrieve the stored RefreshRequest
        val tokenRequest: TokenRequest = workerParams.inputData.getString(TOKEN_REQUEST)
            ?.let { data -> TokenRequest.jsonDeserialize(data) }
            ?: kotlin.run { return Result.failure() }

        // Refresh the access token
        val appAuthController = OpenIdAuthController(application)

        appAuthController.refreshAccessToken(tokenRequest) { response, ex ->
            if (response != null) {
                // Update the stored AuthState with the new token response
                authState.update(response, ex)
                scheduleTokenRefresh(applicationContext, authState)
                // Notify App, save state and pass in the new token to the serviceWorker
                application.onAuthStateChange(authState)
                Log.d(TAG, "Token was updated from the WorkerManager ")
            } else {
                application.onAuthStateChange(authState)
                // Handle failed token refresh here
                scheduleTokenRefresh(applicationContext, authState)
                Log.d(TAG, "Couldn't refresh token ${ex?.message.let { ":  $it" }}")
            }
        }

        return Result.success()
    }
}

fun scheduleTokenRefresh(context: Context, authState: AuthState) {
    if (authState.accessTokenExpirationTime != null) {
        val expiresIn: Long = authState.accessTokenExpirationTime!! - System.currentTimeMillis()
        printTimeInMinutes(expiresIn)
        if (expiresIn > 0) {
            val tokenRefreshWorkRequest = OneTimeWorkRequest.Builder(
                TokenRefreshWorker::class.java
            )
                .setInitialDelay(expiresIn, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(AUTH_STATE, authState.jsonSerializeString())
                        .putString(TOKEN_REQUEST, createTokenRequest(authState))
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "tokenRefreshWork",
                    ExistingWorkPolicy.REPLACE,
                    tokenRefreshWorkRequest
                )
        }
    }
}

fun createTokenRequest(authState: AuthState): String {
    return TokenRequest.Builder(
        OpenIdAuthController.oAuthConfiguration,
        AppConfiguration.oAuthClientId
    )
        .setGrantType(GrantTypeValues.REFRESH_TOKEN)
        .setRefreshToken(authState.refreshToken)
        .setScopes(listOf("openid", "email", "profile", "offline_access"))
        .build()
        .jsonSerializeString()
}

fun printTimeInMinutes(timeInMillis: Long) {
    val minutes = timeInMillis / 60000 // Convert milliseconds to minutes
    Log.i(TokenRefreshWorker.TAG, "Will refresh in: $minutes")
}