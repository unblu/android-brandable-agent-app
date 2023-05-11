package com.unblu.brandeableagentapp.data

import com.unblu.brandeableagentapp.model.AuthenticationType


object AppConfiguration {


    //endPoint

    var unbluServerUrl = "https://testing7.dev.unblu-test.com"
    //var unbluServerUrl = "https://agent-sso-trusted.cloud.unblu-env.com"

    //var unbluServerUrl = "https://brandable-agent-mobile-app.uenv.dev"
    //var unbluServerUrl = "http://10.0.0.7:7777"
    //var unbluServerUrl = "http://10.30.2.44:7777"
    //var unbluServerUrl = "http://10.30.2.44:7777"
    var unbluApiKey = "MZsy5sFESYqU7MawXZgR_w"
    var entryPath = "/co-unblu"
    //var entryPath = "/app"
    var authType: AuthenticationType = AuthenticationType.Direct
    var webAuthProxyServerAddress =  "https://agent-sso-trusted.cloud.unblu-env.com"

}