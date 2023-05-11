package com.unblu.brandeableagentapp.model

sealed class AuthenticationType {
        object Direct : AuthenticationType()
        object OAuth : AuthenticationType()
        object WebProxy  : AuthenticationType()
    }

fun authTypeFromName(string: String) :AuthenticationType{
    return when(string){
        "Direct"-> AuthenticationType.Direct
        "WebProxy"-> AuthenticationType.WebProxy
        "OAuth"-> AuthenticationType.OAuth
        else -> AuthenticationType.Direct
    }
}