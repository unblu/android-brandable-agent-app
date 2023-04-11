package com.unblu.brandeableagentapp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unblu.brandeableagentapp.login.LoginHelper
import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.login.util.validatePassword
import com.unblu.brandeableagentapp.login.util.validateUsername
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.callback.InitializeExceptionCallback
import com.unblu.sdk.core.configuration.UnbluClientConfiguration
import com.unblu.sdk.core.errortype.UnbluClientErrorType
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.log

class LoginViewModel : ViewModel() {
    private lateinit var unbluController: UnbluController
    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState: StateFlow<NavigationState?> = _navigationState
    val _loginState= MutableStateFlow<LoginState>(LoginState.LoggedOut)
    val loginState: StateFlow<LoginState> = _loginState
    val _passwordVisiblity= MutableStateFlow(false)
    val passwordVisiblity: StateFlow<Boolean> = _passwordVisiblity
    val resources = CompositeDisposable()

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
                    cookies?.let {
                        val config  =  UnbluClientConfiguration.Builder(unbluController.getConfiguration()).setCustomCookies(cookies).build()
                        unbluController.start(config, {
                            viewModelScope.launch {
                                _loginState.emit(LoginState.LoggedIn)
                                _navigationState.emit(NavigationState.Success(it.mainView))
                            }
                        },object :InitializeExceptionCallback{
                            override fun onConfigureNotCalled() {
                                _navigationState.value = NavigationState.Failure("Error message: onConfigureNotCalled")
                            }

                            override fun onInErrorState() {
                                _navigationState.value = NavigationState.Failure("Error message: onInErrorState")
                            }

                            override fun onInitFailed(
                                errorType: UnbluClientErrorType,
                                details: String?
                            ) {
                                _navigationState.value = NavigationState.Failure("Error message: $details")
                            }
                        })
                    } ?: kotlin.run {
                        _navigationState.value = NavigationState.Failure("Error message: No authorizaton returned from login")
                    }
                },{ error->
                    _navigationState.value = NavigationState.Failure("Error message: $error")
                })
            }
        }
    }

    fun loginSSO() {
        viewModelScope.launch {
            // Implement your SSO login process here
            // On success:
            //_navigationState.value = NavigationState.Success
            // On failure:
            _navigationState.value = NavigationState.Failure("Error message")
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
                _loginState.emit(LoginState.LoggedOut)
                _navigationState.emit( null)
            }
        }
    }

    fun setPasswordVisiblity(show: Boolean) {
        viewModelScope.launch {
            _passwordVisiblity.emit(show)
        }
    }
}

private fun LoginState?.isLoggingIn(): Boolean {
    Log.w("LOGIN STATE", "will return as we are already loggin")
    return this is LoginState.LoggingIn
}
