package com.unblu.brandeableagentapp

import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.data.Storage.getUnbluSettings
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplication
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.debug.LogLevel

class AgentApplication : UnbluApplication(){
    private var unbluPreferencesStorage: UnbluPreferencesStorage? = null
    private var unbluController : UnbluController? = null

    fun getUnbluPrefs() : UnbluPreferencesStorage {
        if(unbluPreferencesStorage == null) {
            unbluPreferencesStorage = UnbluPreferencesStorage.createSharedPreferencesStorage(this)
            unbluPreferencesStorage?.let { unbluPreferencesStorage->
                AppConfiguration.updateFromSettingsModel(getUnbluSettings(unbluPreferencesStorage))
            }
        }
        return unbluPreferencesStorage!!
    }

    fun getUnbluController() : UnbluController {
        if(unbluController == null) {
            unbluController = UnbluController(this)
        }
        return unbluController!!
    }

    override fun onCreate() {
        super.onCreate()

        getUnbluPrefs()
        Unblu.setLogLevel(LogLevel.DEBUG)
        Unblu.enableDebugOutput = true
        Unblu.onUiVisibilityRequest()
            .subscribe {
                unbluController?.setRequestedUiShow()
            }
    }
}