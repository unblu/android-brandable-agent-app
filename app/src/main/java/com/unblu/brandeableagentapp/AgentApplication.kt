package com.unblu.brandeableagentapp

import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.data.Storage.getUnbluSettings
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplication
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.debug.LogLevel

class AgentApplication : UnbluApplication(){
    var unbluController : UnbluController = UnbluController(this)
    var unbluPreferencesStorage: UnbluPreferencesStorage? = null

    fun getUnbluPrefs() : UnbluPreferencesStorage {
        if(unbluPreferencesStorage == null){
            unbluPreferencesStorage = UnbluPreferencesStorage.createSharedPreferencesStorage(this)
            unbluPreferencesStorage?.let { unbluPreferencesStorage->
                AppConfiguration.updateFromSettingsModel(getUnbluSettings(unbluPreferencesStorage))
            }
        }
        return unbluPreferencesStorage!!
    }
    override fun onCreate() {
        super.onCreate()
        Unblu.setLogLevel(LogLevel.DEBUG)
        Unblu.enableDebugOutput = true
        Unblu.onUiVisibilityRequest()
            .subscribe {
                unbluController.setRequestedUiShow()
            }
    }

}