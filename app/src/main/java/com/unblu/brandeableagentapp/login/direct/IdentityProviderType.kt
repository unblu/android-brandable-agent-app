package com.unblu.brandeableagentapp.login.sso.model

import java.net.URL

sealed class IdentityProviderType {
    object Keycloak : IdentityProviderType()
    object Microsoft : IdentityProviderType()
}

class IdentityProvider(
    val type: IdentityProviderType,
    val webAuthServerAddress: String,
    val webAuthBaseUrl: String,
    val webAuthClientId: String,
    val webAuthClientSecret: String,
    val webAuthCallbackURLScheme: String,
    val webAuthGetTokenId: String,
    val webAuthGetToken: String,
    val webAuthLogout: String,
    val webAuthTenant: String? = null,
    val scope: String
) {

    fun getTokenCodeUrl(): URL {
        var url = webAuthServerAddress
        if (type == IdentityProviderType.Microsoft && webAuthTenant != null) {
            url += "/${webAuthTenant}"
        }
        url += webAuthBaseUrl + webAuthGetTokenId
        if (type == IdentityProviderType.Microsoft) {
            url += "&redirect_uri=${webAuthCallbackURLScheme}%3A%2F%2Fauth"
            url += "&scope=openid%20profile%20email"
        }
        url += "&client_id=$webAuthClientId"
        return URL(url)
    }

    fun getTokenUrl(): URL {
        var url = webAuthServerAddress
        if (type == IdentityProviderType.Microsoft && webAuthTenant != null) {
            url += "/${webAuthTenant}"
        }
        url += webAuthBaseUrl + webAuthGetToken
        return URL(url)
    }

    fun getTokenArguments(
        refreshToken: String? = null,
        oauthCode: String? = null,
        oauthState: String? = null
    ): String {
        var arg = "client_id=$webAuthClientId"
        if (refreshToken != null) {
            arg += "&grant_type=refresh_token"
            arg += "&refresh_token=$refreshToken"
        } else {
            arg += "&grant_type=authorization_code"
            if (type != IdentityProviderType.Microsoft) {
                arg += "&redirect_uri=$webAuthCallbackURLScheme"
                arg += "&state=${oauthState ?: ""}"
                arg += "&client_secret=$webAuthClientSecret"
            }
            arg += "&code=${oauthCode ?: ""}"
        }
        if (type == IdentityProviderType.Microsoft) {
            arg += "&scope=openid%20profile%20email%20offline_access"
            arg += "&redirect_uri=${webAuthCallbackURLScheme}%3A%2F%2Fauth"
        }
        return arg
    }

    fun getLogoutUrl(): URL {
        var url = webAuthServerAddress
        if (type == IdentityProviderType.Microsoft && webAuthTenant != null) {
            url += "/${webAuthTenant}"
        }
        url += webAuthBaseUrl + webAuthLogout
        return URL(url)
    }

    fun getLogoutArguments(refreshToken: String? = null): String {
        var arg = "client_id=$webAuthClientId"
        if (refreshToken != null) {
            arg += "&refresh_token=$refreshToken"
        }
        return arg
    }
}
