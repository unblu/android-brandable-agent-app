package com.unblu.brandeableagentapp.login.sso.oauth
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.unblu.brandeableagentapp.model.TokenEvent
import com.unblu.brandeableagentapp.R
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import io.reactivex.rxjava3.annotations.NonNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.AuthorizationException.GeneralErrors
import net.openid.appauth.AuthorizationService.TokenResponseCallback


class OpenIdAuthController(var context: Context, var storage: UnbluPreferencesStorage) {

    private var authState: AuthState? = null
    private val authService: AuthorizationService = AuthorizationService(context)
    private val _eventReceived = MutableSharedFlow<TokenEvent>()
    val eventReceived: SharedFlow<TokenEvent> = _eventReceived

    init {
        //retrieve auth state or create new instance if nothing on the storage
        authState =  getAuthState(storage)
    }

    private val tokenResponseCallback: TokenResponseCallback = TokenResponseCallback { response, ex ->
        if (response != null) {
            // Update the AuthState with the new token response
            authState?.let { authState->
                authState.update(response, ex)
                storeAuthState(authState, storage)
                if(authState.isAuthorized)
                    scheduleTokenRefresh(context, authState)
            }
            // Handle successful token refresh here
            response.refreshToken?.let {
                CoroutineScope(Dispatchers.Default).launch {
                    _eventReceived.emit(TokenEvent.TokenReceived(it))
                }
            }

            Log.d("MainActivity", " token refreshed: " + response.accessToken)
        } else {
            // Handle failed token refresh here
            Log.e("MainActivity", "Failed to refresh access token", ex)
        }
    }

    fun startSignIn(
        launcher: ActivityResultLauncher<Intent>
    ) {
        val config = AuthorizationServiceConfiguration(
            Uri.parse(AUTH_ENDPOINT),
            Uri.parse(TOKEN_ENDPOINT)
        )
        val authRequestBuilder = AuthorizationRequest.Builder(
            config,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        )
            .setScopes(listOf("openid","email", "profile", "offline_access"))
        val authRequest = authRequestBuilder.build()
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)
    }

    fun handleActivityResult(resultCode: Int, @NonNull data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val authResponse = AuthorizationResponse.fromIntent(data!!)
            val authException = AuthorizationException.fromIntent(data)
            authState = AuthState(authResponse, authException)
            if (authResponse != null) {
                Log.d(TAG,"will request access token")
                requestAccessToken(
                    authResponse,
                    authException
                )
            } else {
                authException?.message?.let { message->
                    Log.w(TAG, message)
                }
                CoroutineScope(Dispatchers.Default).launch {
                    _eventReceived.emit(TokenEvent.ErrorReceived(context.getString(R.string.login_aborted)))
                }
            }
        }else{
            Log.w(TAG, "Login process aborted. It is highly probable that the login page was terminated due to user interaction.")
            CoroutineScope(Dispatchers.Default).launch {
                _eventReceived.emit(TokenEvent.ErrorReceived(context.getString(R.string.login_aborted)))
            }
        }
    }

    private fun requestAccessToken(
        authResponse: AuthorizationResponse?,
        authException: AuthorizationException?,
    ) {
        if (authResponse != null) {
            val tokenRequest = authResponse.createTokenExchangeRequest()
            authService.performTokenRequest(tokenRequest, tokenResponseCallback)
        } else {
            Log.e(TAG, "Authorization failed", authException)
        }
    }

    fun refreshAccessToken(tokenRequest: TokenRequest, callback: TokenResponseCallback) {
        authState?.let { authState ->
            if (authState.refreshToken == null) {
                Log.e(TAG, "No refresh token available")
                callback.onTokenRequestCompleted(null,  GeneralErrors.ID_TOKEN_VALIDATION_ERROR)
                return
            }
            authService.performTokenRequest(tokenRequest, callback)
        }?: run {
            callback.onTokenRequestCompleted(null, AuthorizationException.TokenRequestErrors.OTHER)
        }
    }

    fun logout() {
        // Clear the stored AuthState
        clearAuthState(storage)
        authState = getAuthState(storage)
    }

    companion object {
        private const val CLIENT_ID = "49bf42ce-250d-411b-b150-9ef58e3248bb"
        private const val REDIRECT_URI =
            "msauth://com.unblu.brandeableagentapp/9fs7u0FrIlzqcFJ0wGxh9CRs6iQ%3D"
        private const val AUTH_ENDPOINT =
            "https://login.microsoftonline.com/8005dd54-64b0-4f9d-bf46-e2582d0c2760/oauth2/v2.0/authorize"
        private const val TOKEN_ENDPOINT =
            "https://login.microsoftonline.com/8005dd54-64b0-4f9d-bf46-e2582d0c2760/oauth2/v2.0/token"
         const val TAG = "OpenIdAuthController"
    }
}