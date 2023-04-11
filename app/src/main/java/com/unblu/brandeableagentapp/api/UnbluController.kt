package com.unblu.brandeableagentapp.api

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.agent.UnbluAgentClient
import com.unblu.sdk.core.callback.InitializeExceptionCallback
import com.unblu.sdk.core.callback.InitializeSuccessCallback
import com.unblu.sdk.core.configuration.UnbluClientConfiguration
import com.unblu.sdk.core.configuration.UnbluDownloadHandler
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.internal.visitor.TAG
import com.unblu.sdk.core.links.UnbluPatternMatchingExternalLinkHandler
import com.unblu.sdk.core.model.BackButtonPressTriggerEvent
import com.unblu.sdk.core.notification.UnbluNotificationApi
import com.unblu.sdk.module.call.CallModule
import com.unblu.sdk.module.call.CallModuleProvider
import com.unblu.sdk.module.firebase_notification.UnbluFirebaseNotificationService
import com.unblu.sdk.module.mobilecobrowsing.MobileCoBrowsingModule
import com.unblu.sdk.module.mobilecobrowsing.MobileCoBrowsingModuleProvider
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*

class UnbluController(var agentApplication: Context) {
    private var unbluClientConfiguration: UnbluClientConfiguration
    private lateinit var unbluPreferencesStorage: UnbluPreferencesStorage

    init {
        unbluClientConfiguration = createUnbluClientConfiguration(agentApplication)
    }

    companion object {
        const val PREFERENCES = "TestApp"
        const val KEY_ACCESS_TOKEN = "ACCESS_TOKEN"
        const val KEY_ACCESS_TOKEN_ACTIVE = "ACCESS_TOKEN_ACTIVE"
    }

    //Store uiShowRequests as you may receive them when you don't have a client running
    //or the Unblu Ui isn't attached
    private var onUiShowRequest = BehaviorSubject.createDefault(false)
    private var agentClient: UnbluAgentClient? = null
    private lateinit var callModule: CallModule
    private lateinit var coBrowsingModule: MobileCoBrowsingModule
    private var unbluNotificationApi: UnbluNotificationApi =
        UnbluFirebaseNotificationService.getNotificationApi()

    fun start(
        config: UnbluClientConfiguration,
        successVoidCallback: InitializeSuccessCallback<UnbluAgentClient>,
        deinitializeExceptionCallback: InitializeExceptionCallback
    ) {
        unbluClientConfiguration = config
        //create your Client Instance
        createClient(successVoidCallback, deinitializeExceptionCallback)
    }

    private fun createClient(
        successCallback: InitializeSuccessCallback<UnbluAgentClient>,
        initializeExceptionCallback: InitializeExceptionCallback
    ) {
        Unblu.createAgentClient(
            agentApplication as Application,
            unbluClientConfiguration,
            unbluNotificationApi,
            {
                agentClient = it
                successCallback.onSuccess(it)
            },
            initializeExceptionCallback
        )
    }

    private fun createUnbluClientConfiguration (uApplication: Context): UnbluClientConfiguration {
        val url = "https://testing7.dev.unblu-test.com"
        val apiKey = "MZsy5sFESYqU7MawXZgR_w"
        unbluPreferencesStorage = UnbluPreferencesStorage.createSharedPreferencesStorage(agentApplication)
        callModule = CallModuleProvider.create()
        coBrowsingModule = MobileCoBrowsingModuleProvider.create()
        return UnbluClientConfiguration.Builder(
            url,
            apiKey,
            unbluPreferencesStorage,
            UnbluDownloadHandler.createExternalStorageDownloadHandler(uApplication as Application),
            UnbluPatternMatchingExternalLinkHandler()
        )
            .setEntryPath("/co-unblu")
            .registerModule(callModule)
            .registerModule(coBrowsingModule).build()
    }

    fun isAccessTokenActive(): Boolean {
        return unbluPreferencesStorage.get(KEY_ACCESS_TOKEN_ACTIVE)?.let { it.toBoolean() }
            ?: false
    }

    fun getClient(): UnbluAgentClient? {
        return if (Objects.isNull(agentClient) || (agentClient!!.isDeInitialized)) null else agentClient
    }

    fun getCallModule(): CallModule {
        return callModule
    }

    fun getCoBrowsingModule(): MobileCoBrowsingModule {
        return coBrowsingModule
    }

    fun getUnbluUi(): View? {
        return agentClient?.mainView
    }

    fun setRequestedUiShow() {
        onUiShowRequest.onNext(true)
    }

    fun hasUiShowRequest(): Observable<Boolean> {
        return onUiShowRequest
    }

    fun getHasUiShowRequestValue(): Boolean {
        return onUiShowRequest.value!!
    }

    fun clearUiShowRequest() {
        onUiShowRequest.onNext(false)
    }

    fun getHasUiShowRequestValueAndReset(): Boolean {
        val showUiVal = onUiShowRequest.value!!
        clearUiShowRequest()
        return showUiVal
    }

    fun getConfiguration(): UnbluClientConfiguration {
        return unbluClientConfiguration
    }

    fun stop(onStopped: () -> Unit) {
        agentClient?.let { agentClient ->
            agentClient.deinitClient({
                onStopped()
            },  {
                Log.e("UnbluController", "Error deInitializing: $it")
            })
        } ?: run(onStopped)
        agentClient = null
    }
}