package com.unblu.brandeableagentapp.data

import com.unblu.brandeableagentapp.model.AuthenticationType


object AppConfiguration {
    //endPoint
    var unbluServerUrl = "https://testing7.dev.unblu-test.com"

    //var unbluServerUrl = "https://agent-sso-trusted.cloud.unblu-env.com"
    //var unbluServerUrl = "http://10.30.2.44:7777"
    //var unbluServerUrl = "http://10.0.0.7:7777"
    //var unbluServerUrl = "https://brandable-agent-mobile-app.uenv.dev"

    var unbluApiKey = "MZsy5sFESYqU7MawXZgR_w"
    //var unbluApiKey = "IzkRDlr6QtKIZ7tQBfz5sw"

    var entryPath = "/co-unblu"
    //var entryPath = "/app"
    var authType: AuthenticationType = AuthenticationType.Direct

    //WebProxy
    var webAuthProxyServerAddress =  "https://agent-sso-trusted.cloud.unblu-env.com"

    //OAuth
    val oAuthClientId = "aae6ad6b-2230-414e-83f0-2b5933499b0b"
    val oAuthRedirectUri = "msauth://com.unblu.brandeableagentapp/9fs7u0FrIlzqcFJ0wGxh9CRs6iQ%3D"
    val oAuthEndpoint =
        "https://login.microsoftonline.com/8005dd54-64b0-4f9d-bf46-e2582d0c2760/oauth2/v2.0/authorize"
    val oAuthTokenEndpoint =
        "https://login.microsoftonline.com/8005dd54-64b0-4f9d-bf46-e2582d0c2760/oauth2/v2.0/token"
}