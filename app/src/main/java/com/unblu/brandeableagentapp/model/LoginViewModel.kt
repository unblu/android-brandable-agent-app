package com.unblu.brandeableagentapp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.direct.LoginHelper
import com.unblu.brandeableagentapp.login.direct.util.validatePassword
import com.unblu.brandeableagentapp.login.direct.util.validateUsername
import com.unblu.brandeableagentapp.login.proxy.ProxyWebViewClient
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

class LoginViewModel : ViewModel() {
    private lateinit var unbluController: UnbluController
    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState: StateFlow<NavigationState?> = _navigationState
    private val _loginState= MutableStateFlow<LoginState>(LoginState.LoggedOut)
    val loginState: StateFlow<LoginState> = _loginState
    private val resources = CompositeDisposable()
    private val _customTabsOpen = MutableStateFlow(false)
    val customTabsOpen: StateFlow<Boolean> = _customTabsOpen

    private val _showWebview = MutableStateFlow(false)
    val showWebview: StateFlow<Boolean> = _showWebview

    private val _authType = MutableStateFlow(AppConfiguration.authType)
    val authType: StateFlow<AppConfiguration.AuthenticationType> = _authType

    //Direct login
    private val _passwordVisiblity= MutableStateFlow(false)
    val passwordVisiblity: StateFlow<Boolean> = _passwordVisiblity

    //SSO proxy login
    val onCookieReceived :(Set<UnbluCookie>?) -> Unit = { cookies->
        startUnblu(cookies)
    }

    private fun startUnblu(cookies: Set<UnbluCookie>?) {
        cookies?.let {
            val config = UnbluClientConfiguration.Builder(unbluController.getConfiguration())
                .setCustomCookies(cookies).build()
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
        } ?: kotlin.run {
            _navigationState.value =
                NavigationState.Failure("Error message: No authorizaton returned from login")
            resetSSOLogin()
        }
    }

    init {
        resources.add(Unblu.onError().subscribe {
            NavigationState.Failure("Error message: Check ${it.message}")
            viewModelScope.launch {
                delay(2000)
                _navigationState.emit(null)
            }
        })
    }

    fun login(username: String, password: String) {
        if(loginState.value.isLoggingIn()) return
        val user = validateUsername(username)
         if(!user || !validatePassword(password)){
            val wrongData = if(!user) "Username" else "Password"
            _navigationState.value = NavigationState.Failure("Error message: Check $wrongData")
        }else{
            viewModelScope.launch {
                _loginState.emit(LoginState.LoggingIn)
                LoginHelper.login(unbluController.getConfiguration(),username, password, { cookies->
                    startUnblu(cookies)
                },{ error->
                    _navigationState.value = NavigationState.Failure("Error message: $error")
                })
            }
        }
    }

    fun launchSSO() {
        viewModelScope.launch {
            if(authType.value is AppConfiguration.AuthenticationType.OAuth)
                _customTabsOpen.emit(true)
            else
                _showWebview.emit(true)
        }
    }

    fun setUnbluController(unbluController: UnbluController) {
        this.unbluController = unbluController
    }

    override fun onCleared() {
        resources.clear()
        super.onCleared()
    }

    fun stopUnblu() {
        unbluController.stop {
            viewModelScope.launch {
                doResetLoginState()
            }
        }
    }

    fun setPasswordVisiblity(show: Boolean) {
        viewModelScope.launch {
            _passwordVisiblity.emit(show)
        }
    }

    private suspend fun doResetLoginState() {
            _navigationState.emit( null)
            _loginState.emit(LoginState.LoggedOut)
    }

    fun resetSSOLogin() {
        viewModelScope.launch {
            _customTabsOpen.emit(false)
            _showWebview.emit(false)
            _navigationState.emit(null)
        }
    }
}

private fun LoginState?.isLoggingIn(): Boolean {
    Log.w("LOGIN STATE", "will return as we are already loggin")
    return this is LoginState.LoggingIn
}
