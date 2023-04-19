package com.unblu.brandeableagentapp.data

import com.unblu.brandeableagentapp.login.sso.model.IdentityProvider
import com.unblu.brandeableagentapp.login.sso.model.IdentityProviderType

object AppConfiguration {
    sealed class AuthenticationType {
        object Direct : AuthenticationType()
        object OAuth : AuthenticationType()
        object OAuthProxy  : AuthenticationType()
    }

    //endPoint
    var unbluServerUrl = "https://agent-sso-trusted.cloud.unblu-env.com"
    //var unbluApiKey = "MZsy5sFESYqU7MawXZgR_w"
    var unbluApiKey = "IzkRDlr6QtKIZ7tQBfz5sw"
    var authType: AuthenticationType = AuthenticationType.OAuthProxy
    var authProvider: IdentityProviderType = IdentityProviderType.Keycloak

    var webAuthProxyServerAddress =  "https://agent-sso-trusted.cloud.unblu-env.com"

    val authProviders: Map<IdentityProviderType, IdentityProvider> = mapOf(
        IdentityProviderType.Keycloak to IdentityProvider(
            type = IdentityProviderType.Keycloak,
            webAuthServerAddress = "https://agent-sso-trusted.cloud.unblu-env.com",
             webAuthBaseUrl = "/realms/TestRealm/protocol/openid-connect",
            webAuthClientId = "ios-app-1",
            webAuthClientSecret = "sfaCF7Gpo5iaqF0rcC4Ijfxe5SQOEyK1",
            webAuthCallbackURLScheme = "https://agent-sso-trusted.cloud.unblu-env.com/oauth2/callback",
            webAuthGetTokenId = "/auth",
            webAuthGetToken = "/token",
            webAuthLogout = "/logout",
            scope="openid profile email"
        ),
        IdentityProviderType.Microsoft to IdentityProvider(
            type = IdentityProviderType.Microsoft,
            webAuthServerAddress = "https://login.microsoftonline.com",
            webAuthBaseUrl = "/oauth2/v2.0",
            webAuthClientId = "aae6ad6b-2230-414e-83f0-2b5933499b0b",
            webAuthClientSecret = "VJ38Q~r0i77qACoCtdN~dig9XsYPFrT-5mZadaef",
            webAuthCallbackURLScheme = "com.unblu.brandeableagentapp",
            webAuthGetTokenId = "/authorize",
            webAuthGetToken = "/token",
            webAuthLogout = "/logout",
            webAuthTenant = "8005dd54-64b0-4f9d-bf46-e2582d0c2760",
            scope="openid profile email offline_access"
        )
    )

}