package com.unblu.brandeableagentapp.model

import com.unblu.brandeableagentapp.data.AppConfiguration

data class SettingsModel(
    var unbluServerUrl: String = AppConfiguration.unbluServerUrl,
    var unbluApiKey: String =  AppConfiguration.unbluApiKey,
    var entryPath: String = AppConfiguration.entryPath,
    var authType: AuthenticationType = AppConfiguration.authType,
    var webAuthProxyServerAddress : String = AppConfiguration.webAuthProxyServerAddress,
    val oAuthClientId: String = AppConfiguration.oAuthClientId,
    val oAuthRedirectUri: String = AppConfiguration.oAuthRedirectUri,
    val oAuthEndpoint: String = AppConfiguration.oAuthEndpoint,
    val oAuthTokenEndpoint: String = AppConfiguration.oAuthTokenEndpoint
)