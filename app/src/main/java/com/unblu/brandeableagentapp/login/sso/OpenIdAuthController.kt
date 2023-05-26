package com.unblu.brandeableagentapp.login.sso

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.data.AppConfiguration.oAuthClientId
import com.unblu.brandeableagentapp.data.AppConfiguration.oAuthEndpoint
import com.unblu.brandeableagentapp.data.AppConfiguration.oAuthRedirectUri
import com.unblu.brandeableagentapp.data.AppConfiguration.oAuthTokenEndpoint
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.TokenEvent
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.internal.utils.Logger
import io.reactivex.rxjava3.annotations.NonNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.openid.appauth.*
import net.openid.appauth.AuthorizationException.GeneralErrors
import net.openid.appauth.AuthorizationService.TokenResponseCallback

/**
 *  *This class can be deleted in case the selected [AuthenticationType] is not [AuthenticationType.OAuth].
 *
 *  Controller for login/authentication through OAuth
 *
 * @property context Context
 * @property storage UnbluPreferencesStorage
 * @property authState AuthState?
 * @property authService AuthorizationService
 * @property _eventReceived MutableSharedFlow<TokenEvent>
 * @property eventReceived SharedFlow<TokenEvent>
 * @property tokenResponseCallback TokenResponseCallback
 * @constructor
 */
class OpenIdAuthController(var context: Context, var storage: UnbluPreferencesStorage) {

    private var authState: AuthState? = null
    private val authService: AuthorizationService
    private val _eventReceived = MutableSharedFlow<TokenEvent>()
    val eventReceived: SharedFlow<TokenEvent> = _eventReceived

    init {
        //retrieve auth state or create new instance if nothing on the storage
        authState = getAuthState(storage)
        authService = AuthorizationService(context)
    }

    private val tokenResponseCallback: TokenResponseCallback =
        TokenResponseCallback { response, ex ->
            if (response != null) {
                // Update the AuthState with the new token response
                authState?.let { authState ->
                    authState.update(response, ex)
                    authState.accessToken?.let { accessToken ->
                        if (authState.isAuthorized)
                            scheduleTokenRefresh(context, authState)
                        // Handle successful token refresh here
                        CoroutineScope(Dispatchers.Default).launch {
                            Logger.d(TAG, "accessToken: $accessToken")
                            _eventReceived.emit(TokenEvent.TokenReceived(accessToken))
                        }
                    }
                }
                Log.d("MainActivity", " token refreshed: " + response.refreshToken)
            } else {
                // Handle failed token refresh here
                Log.e("MainActivity", "Failed to refresh access token", ex)
                CoroutineScope(Dispatchers.Default).launch {
                    _eventReceived.emit(
                        TokenEvent.ErrorReceived(
                            ex?.message ?: "Error retrieving oauth token "
                        )
                    )
                }
            }
        }

    fun startSignIn(
        launcher: ActivityResultLauncher<Intent>
    ) {
        if (shouldReAuth()) {
            Log.d(TAG, "Authentication needed")
            val authRequest = AuthorizationRequest.Builder(
                oAuthConfiguration,
                oAuthClientId,
                ResponseTypeValues.CODE,
                Uri.parse(oAuthRedirectUri)
            )
                .setScopes(listOf("openid", "email", "profile"))
                .build();
            val authIntent = authService.getAuthorizationRequestIntent(authRequest)
            launcher.launch(authIntent)
        } else {
            Log.i(TAG, "Token still valid, will use it")
            CoroutineScope(Dispatchers.Default).launch {
                authState?.accessToken?.let {
                    _eventReceived.emit(TokenEvent.TokenReceived(it));
                }
            }
        }
    }

    private fun shouldReAuth(): Boolean {
        return authState?.accessTokenExpirationTime?.let { time -> time - System.currentTimeMillis() <= 0 } ?: true
    }

    fun handleActivityResult(resultCode: Int, @NonNull data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val authResponse = AuthorizationResponse.fromIntent(data!!)
            val authException = AuthorizationException.fromIntent(data)
            authState = AuthState(authResponse, authException)
            if (authResponse != null) {
                Log.d(TAG, "will request access token")
                requestAccessToken(
                    authResponse,
                    authException
                )
            } else {
                authException?.message?.let { message ->
                    Log.w(TAG, message)
                }
                CoroutineScope(Dispatchers.Default).launch {
                    _eventReceived.emit(TokenEvent.ErrorReceived(context.getString(R.string.login_aborted)))
                }
            }
        } else {
            Log.w(
                TAG,
                "Login process aborted. It is highly probable that the login page was terminated due to user interaction."
            )
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
            authService.performTokenRequest(
                createTokenExchangeRequest(authResponse),
                tokenResponseCallback
            )
        } else {
            Log.e(TAG, "Authorization failed", authException)
            tokenResponseCallback.onTokenRequestCompleted(null, authException)
        }
    }

    private fun createTokenExchangeRequest(authResponse: AuthorizationResponse): TokenRequest {
        return TokenRequest.Builder(
            oAuthConfiguration,
            oAuthClientId
        ).setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
            .setAuthorizationCode(authResponse.authorizationCode)
            .setCodeVerifier(authResponse.request.codeVerifier)
            .setNonce(authResponse.request.nonce)
            .setRedirectUri(Uri.parse(oAuthRedirectUri))
            .setScopes(listOf("openid", "email", "profile", "offline_access"))
            .build()
    }

    fun refreshAccessToken(tokenRequest: TokenRequest, callback: TokenResponseCallback) {
        authState?.let { authState ->
            if (authState.refreshToken == null) {
                Log.e(TAG, "No refresh token available")
                callback.onTokenRequestCompleted(null, GeneralErrors.ID_TOKEN_VALIDATION_ERROR)
                return
            }
            authService.performTokenRequest(tokenRequest, callback)
        } ?: run {
            callback.onTokenRequestCompleted(null, AuthorizationException.TokenRequestErrors.OTHER)
        }
    }

    companion object {
        const val TAG = "OpenIdAuthController"
        val oAuthConfiguration = AuthorizationServiceConfiguration(
            Uri.parse(oAuthEndpoint),
            Uri.parse(oAuthTokenEndpoint)
        )

    }
}