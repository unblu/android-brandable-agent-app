package com.unblu.brandeableagentapp

import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.data.Storage.getUnbluSettings
import com.unblu.brandeableagentapp.login.sso.storeAuthState
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplication
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState

class AgentApplication : UnbluApplication(){
    private val _onTokenUpdate: MutableSharedFlow<String> = MutableSharedFlow(replay = 0)
    val onTokenUpdate: SharedFlow<String> = _onTokenUpdate
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

    fun onAuthStateChange(authState: AuthState) {
        unbluPreferencesStorage?.let { storeAuthState(authState, it) }
        CoroutineScope(Dispatchers.Default).launch {
            authState.accessToken?.let { token->_onTokenUpdate.emit(token) }
        }
    }
}