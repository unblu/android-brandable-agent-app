package com.unblu.brandeableagentapp

import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplication
import com.unblu.sdk.core.debug.LogLevel

class AgentApplication : UnbluApplication(){
    var unbluController : UnbluController = UnbluController(this)

    override fun onCreate() {
        super.onCreate()
        Unblu.setLogLevel(LogLevel.WARN)
        Unblu.onUiVisibilityRequest()
            .subscribe {
                unbluController.setRequestedUiShow()
            }
    }

}