package com.unblu.brandeableagentapp.model

sealed class LoginState {
    object LoggedOut : LoginState()
    object LoggedIn : LoginState()
    object LoggingIn : LoginState()
}

