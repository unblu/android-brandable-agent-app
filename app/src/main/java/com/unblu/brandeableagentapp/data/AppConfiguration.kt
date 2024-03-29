package com.unblu.brandeableagentapp.data

import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.SettingsModel


object AppConfiguration {


    var unbluServerUrl = "https://testing7.dev.unblu-test.com"
    var unbluApiKey = "MZsy5sFESYqU7MawXZgR_w"
    var entryPath = "/co-unblu"

    var authType: AuthenticationType = AuthenticationType.Direct
    //WebProxy
    var webAuthProxyServerAddress =  "https://agent-sso-trusted.cloud.unblu-env.com"
    //OAuth
    var oAuthClientId = "aae6ad6b-2230-414e-83f0-2b5933499b0b"
    var oAuthRedirectUri = "msauth://com.unblu.brandeableagentapp/9fs7u0FrIlzqcFJ0wGxh9CRs6iQ%3D"
    var oAuthEndpoint =
        "https://login.microsoftonline.com/8005dd54-64b0-4f9d-bf46-e2582d0c2760/oauth2/v2.0/authorize"
    var oAuthTokenEndpoint =
        "https://login.microsoftonline.com/8005dd54-64b0-4f9d-bf46-e2582d0c2760/oauth2/v2.0/token"

    fun updateFromSettingsModel(settingsModel: SettingsModel){
           unbluServerUrl = settingsModel.unbluServerUrl
           unbluApiKey = settingsModel.unbluApiKey
           entryPath = settingsModel.entryPath
           authType = settingsModel.authType
           webAuthProxyServerAddress = settingsModel.webAuthProxyServerAddress
            oAuthClientId = settingsModel.oAuthClientId
            oAuthRedirectUri = settingsModel.oAuthRedirectUri
            oAuthEndpoint = settingsModel.oAuthEndpoint
            oAuthTokenEndpoint = settingsModel.oAuthTokenEndpoint
    }
}