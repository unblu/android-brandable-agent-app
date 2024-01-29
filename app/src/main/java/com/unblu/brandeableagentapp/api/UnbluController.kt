package com.unblu.brandeableagentapp.api

import android.app.Application
import android.util.Log
import android.view.View
import com.unblu.brandeableagentapp.AgentApplication
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.livekitmodule.LiveKitModuleProvider
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.agent.UnbluAgentClient
import com.unblu.sdk.core.callback.InitializeExceptionCallback
import com.unblu.sdk.core.callback.InitializeSuccessCallback
import com.unblu.sdk.core.configuration.UnbluClientConfiguration
import com.unblu.sdk.core.configuration.UnbluDownloadHandler
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.links.UnbluPatternMatchingExternalLinkHandler
import com.unblu.sdk.core.module.call.CallModuleProviderFactory
import com.unblu.sdk.core.notification.UnbluNotificationApi
import com.unblu.sdk.module.call.CallModule
import com.unblu.sdk.module.call.CallModuleProvider
import com.unblu.sdk.module.mobilecobrowsing.MobileCoBrowsingModule
import com.unblu.sdk.module.mobilecobrowsing.MobileCoBrowsingModuleProvider
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*

class UnbluController(
    var agentApplication: AgentApplication
) {
    private val unbluDownloadHandler: UnbluDownloadHandler = UnbluDownloadHandler.createExternalStorageDownloadHandler(agentApplication)
    private var unbluClientConfiguration: UnbluClientConfiguration = createUnbluClientConfiguration()

    //Store uiShowRequests as you may receive them when you don't have a client running
    //or the Unblu Ui isn't attached
    private var onUiShowRequest = BehaviorSubject.createDefault(false)
    private var agentClient: UnbluAgentClient? = null
    private lateinit var callModule: CallModule
    private lateinit var coBrowsingModule: MobileCoBrowsingModule
    private var unbluNotificationApi: UnbluNotificationApi =
        //Use line below if you will be using UnbluFirebaseNotificationService to receive push notifications. Also make sure you have your google-services.json in the app/ folder.
        //UnbluFirebaseNotificationService.getNotificationApi()
        UnbluNotificationApi.createNotificationApi()

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

    private fun createUnbluClientConfiguration (): UnbluClientConfiguration {
        callModule = CallModuleProviderFactory.createDynamic(
            CallModuleProvider.createForDynamic(),
            LiveKitModuleProvider.createForDynamic()
        )
        coBrowsingModule = MobileCoBrowsingModuleProvider.create()
        return UnbluClientConfiguration.Builder(
            AppConfiguration.unbluServerUrl,
            AppConfiguration.unbluApiKey,
            agentApplication.getUnbluPrefs(),
            unbluDownloadHandler,
            UnbluPatternMatchingExternalLinkHandler()
        )
            .setEntryPath(AppConfiguration.entryPath)
            //.setInternalUrlPatternWhitelist(listOf(Pattern.compile("https://agent-sso-trusted\\.cloud\\.unblu-env\\.com")))
            .registerModule(callModule)
            .registerModule(coBrowsingModule).build()
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

    fun clearUiShowRequest() {
        onUiShowRequest.onNext(false)
    }

    fun getHasUiShowRequestValueAndReset(): Boolean {
        val showUiVal = onUiShowRequest.value!!
        clearUiShowRequest()
        return showUiVal
    }

    fun getConfiguration(): UnbluClientConfiguration {
        createUnbluClientConfiguration()
        unbluClientConfiguration =
            UnbluClientConfiguration.Builder(unbluClientConfiguration)
                .setUnbluBaseUrl(AppConfiguration.unbluServerUrl)
                .setApiKey(AppConfiguration.unbluApiKey)
                .setEntryPath(AppConfiguration.entryPath)
                .build()
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

    fun setOAuthToken(token: String) {
        unbluClientConfiguration = UnbluClientConfiguration.Builder(unbluClientConfiguration).setOAuthToken(token).build()
    }

    fun getPreferencesStorage(): UnbluPreferencesStorage {
        return unbluClientConfiguration.preferencesStorage
    }
}