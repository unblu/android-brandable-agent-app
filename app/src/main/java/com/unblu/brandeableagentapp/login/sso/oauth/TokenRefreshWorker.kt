package com.unblu.brandeableagentapp.login.sso.oauth

import android.content.Context
import android.util.Log
import androidx.work.*
import com.unblu.brandeableagentapp.AgentApplication
import com.unblu.brandeableagentapp.login.sso.oauth.TokenRefreshWorker.Companion.TOKEN_REQUEST
import net.openid.appauth.AuthState
import net.openid.appauth.TokenRequest
import java.util.concurrent.TimeUnit


class TokenRefreshWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    companion object{
        const val TAG = "TokenRefreshWorker"
        const val TOKEN_REQUEST = "token_request"
    }
    private var workerParams: WorkerParameters

    init {
        this.workerParams = workerParams
    }

    override fun doWork(): Result {
        val unbluController = (applicationContext as AgentApplication).unbluController
        // Retrieve the stored AuthState
        val authState: AuthState = workerParams.inputData.getString(AUTH_STATE)
            ?.let { data -> AuthState.jsonDeserialize(data) }
            ?: kotlin.run { return Result.failure() }
        // Retrieve the stored RefreshRequest
        val tokenRequest: TokenRequest = workerParams.inputData.getString(TOKEN_REQUEST)
            ?.let { data -> TokenRequest.jsonDeserialize(data) }
            ?: kotlin.run { return Result.failure() }

        val unbluPreferencesStorage = (applicationContext as AgentApplication).getUnbluPrefs()

        // Refresh the access token
        val appAuthController =
            OpenIdAuthController(applicationContext, unbluPreferencesStorage)

        appAuthController.refreshAccessToken(tokenRequest) { response, ex ->
            if (response != null) {
                // Update the stored AuthState with the new token response
                authState.update(response, ex)
                storeAuthState(authState, unbluPreferencesStorage)
                scheduleTokenRefresh(applicationContext, authState)
                unbluController.getClient()?.apply {
                    authState.refreshToken?.let {
                        setAccessToken(authState.refreshToken)
                    }?: kotlin.run {
                        Log.w(TAG, "did not receive refresh token")
                    }
                }
                // Notify App and pass in the new token to the serviceWorker
            } else {
                scheduleTokenRefresh(applicationContext, authState)
                // Handle failed token refresh here
            }
        }

        return Result.success()
    }
}

fun scheduleTokenRefresh(context: Context, authState: AuthState) {
    if (authState.accessTokenExpirationTime != null) {
        val expiresIn: Long = authState.accessTokenExpirationTime!! - System.currentTimeMillis()
        if (expiresIn > 0) {
            val tokenRefreshWorkRequest = OneTimeWorkRequest.Builder(
                TokenRefreshWorker::class.java
            )
                .setInitialDelay(expiresIn, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(AUTH_STATE, authState.jsonSerializeString())
                        .putString(TOKEN_REQUEST, authState.createTokenRefreshRequest().jsonSerializeString()).build()
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