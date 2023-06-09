package com.unblu.brandeableagentapp.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.direct.LoginHelper
import com.unblu.brandeableagentapp.login.direct.util.validatePassword
import com.unblu.brandeableagentapp.login.direct.util.validateUsername
import com.unblu.brandeableagentapp.data.Storage.UNBLU_USERNAME
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.callback.InitializeExceptionCallback
import com.unblu.sdk.core.configuration.UnbluClientConfiguration
import com.unblu.sdk.core.configuration.UnbluCookie
import com.unblu.sdk.core.errortype.UnbluClientErrorType
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * edit this class aaccording to the  [AuthenticationType] that will be used:
 *  if you are using [AuthenticationType.OAuth]  you should only keep the following properties and methods:
 *  [LoginViewModel.customTabsOpen]
 *  [LoginViewModel._customTabsOpen]
 * [LoginViewModel.resetSSOLogin]
 * [LoginViewModel.launchSSO]
 *
 * if you are using [AuthenticationType.Direct]:
 *  [LoginViewModel.password]
 *  [LoginViewModel.username]
 *  [LoginViewModel.passwordVisiblity]
 *  [LoginViewModel._passwordVisiblity]
 *  [LoginViewModel.startUnblu]
 *  [LoginViewModel.login]
 *  [LoginViewModel.setPasswordVisiblity]
 *
 * Finalliy if you are using [AuthenticationType.WebProxy]
 *  [LoginViewModel.startUnblu]
 *  [LoginViewModel._showWebview]
 *  [LoginViewModel.showWebview]
 *  [LoginViewModel.onCookieReceived]
 *  [LoginViewModel.launchSSO]
 *
 *  then you can delete the properties/methods that refer to the other authentication types and it's subsequent references.
 */
class LoginViewModel : ViewModel() {
    private lateinit var unbluController: UnbluController
    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState: StateFlow<NavigationState?> = _navigationState
    private val _loginState = MutableStateFlow<LoginState>(LoginState.LoggedOut)
    val loginState: StateFlow<LoginState> = _loginState
    private val resources = CompositeDisposable()

    //oAuth
    private val _customTabsOpen = MutableStateFlow(false)
    val customTabsOpen: StateFlow<Boolean> = _customTabsOpen

    //WebProxy
    private val _showWebview = MutableStateFlow(false)
    val showWebview: StateFlow<Boolean> = _showWebview
    val onCookieReceived: (Set<UnbluCookie>?) -> Unit = { cookies ->
        startUnblu(cookies)
    }
    //Direct
    val password = mutableStateOf("harmless-squire-spotter")
    val username = mutableStateOf("superadmin")

    //Direct login
    private val _passwordVisiblity = MutableStateFlow(false)
    val passwordVisiblity: StateFlow<Boolean> = _passwordVisiblity

    fun startUnblu(cookies: Set<UnbluCookie>?) {
        viewModelScope.launch {
            _showWebview.emit(false)
        }
        val config = cookies?.let {
            UnbluClientConfiguration.Builder(unbluController.getConfiguration())
                .setUnbluBaseUrl(AppConfiguration.unbluServerUrl)
                .setApiKey(AppConfiguration.unbluApiKey)
                .setEntryPath(AppConfiguration.entryPath)
                .setCustomCookies(cookies).build()
        } ?: unbluController.getConfiguration()

        unbluController.start(config, {
            viewModelScope.launch {
                _loginState.emit(LoginState.LoggedIn)
                _navigationState.emit(NavigationState.Success(it.mainView))
            }
        }, object : InitializeExceptionCallback {
            override fun onConfigureNotCalled() {
                _navigationState.value =
                    NavigationState.Failure("Error message: onConfigureNotCalled")
                resetSSOLogin()
            }

            override fun onInErrorState() {
                _navigationState.value =
                    NavigationState.Failure("Error message: onInErrorState")
                resetSSOLogin()
            }

            override fun onInitFailed(
                errorType: UnbluClientErrorType,
                details: String?
            ) {
                _navigationState.value = NavigationState.Failure("Error message: $details")
                resetSSOLogin()
            }
        })

    }

    init {
        resources.add(Unblu.onError().subscribe {
            NavigationState.Failure("Error message: Check ${it.message}")
            viewModelScope.launch {
                delay(500)
                _navigationState.emit(null)
            }
        })
    }


    fun login(username: String, password: String) {
        if (loginState.value.isLoggingIn()) return
        val user = validateUsername(username)
        if (!user || !validatePassword(password)) {
            val wrongData = if (!user) "Username" else "Password"
            _navigationState.value = NavigationState.Failure("Error message: Check $wrongData")
        } else {
            viewModelScope.launch {
                _loginState.emit(LoginState.LoggingIn)
                LoginHelper.login(
                    unbluController.getConfiguration(),
                    username,
                    password,
                    { cookies ->
                        unbluController.getPreferencesStorage().put(UNBLU_USERNAME, username)
                        startUnblu(cookies)
                    },
                    { error ->
                        _navigationState.value = NavigationState.Failure("Error message: $error")
                        _loginState.value = LoginState.LoggedOut
                    })
            }
        }
    }

    fun onPasswordChange(newPassword: String) {
        password.value = newPassword
    }

    fun onUsernameChange(newUsername: String) {
        username.value = newUsername
    }

    fun setPasswordVisiblity(show: Boolean) {
        viewModelScope.launch {
            _passwordVisiblity.emit(show)
        }
    }

    fun launchSSO() {
        viewModelScope.launch {
            _loginState.emit(LoginState.LoggingIn)
        }
        viewModelScope.launch {
            /**
             * edit this code aaccording to the  [AuthenticationType]
             *  if you are using [AuthenticationType.OAuth] replace it with :
             *     _customTabsOpen.emit(true)
             *
             * Otherwise replace it with:
             *   _showWebview.emit(true)
             */

            if (AppConfiguration.authType is AuthenticationType.OAuth)
                _customTabsOpen.emit(true)
            else
                _showWebview.emit(true)
        }
    }

    fun resetSSOLogin() {
        viewModelScope.launch {
            _loginState.emit(LoginState.LoggedOut)
            _customTabsOpen.emit(false)
            _showWebview.emit(false)
            _navigationState.emit(null)
        }
    }

    override fun onCleared() {
        resources.clear()
        super.onCleared()
    }

    private suspend fun doResetLoginState() {
        _navigationState.emit(null)
        _loginState.emit(LoginState.LoggedOut)
    }

    fun stopUnblu() {
        unbluController.stop {
            viewModelScope.launch {
                doResetLoginState()
            }
        }
    }

    fun setUnbluController(unbluController: UnbluController) {
        this.unbluController = unbluController
        if (AppConfiguration.authType == AuthenticationType.Direct)
            unbluController.getPreferencesStorage().get(UNBLU_USERNAME)?.apply {
                onUsernameChange(this)
            }
    }

    fun setErrorMessage(message: String) {
        _navigationState.value = NavigationState.Failure("Error message: $message")
    }
}

private fun LoginState?.isLoggingIn(): Boolean {
    Log.d("LOGIN STATE", "will return as we are already loggin")
    return this is LoginState.LoggingIn
}
