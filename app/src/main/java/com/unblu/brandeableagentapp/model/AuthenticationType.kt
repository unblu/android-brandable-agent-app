package com.unblu.brandeableagentapp.model

sealed class AuthenticationType(var name: String) {
        object Direct : AuthenticationType("Direct")
        object OAuth : AuthenticationType("OAuth")
        object WebProxy  : AuthenticationType("WebProxy")
    }

fun authTypeFromName(string: String) :AuthenticationType{
    return when(string){
        AuthenticationType.Direct.name-> AuthenticationType.Direct
        AuthenticationType.WebProxy.name -> AuthenticationType.WebProxy
        AuthenticationType.OAuth.name-> AuthenticationType.OAuth
        else -> AuthenticationType.Direct
    }
}