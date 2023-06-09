package com.unblu.brandeableagentapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.sso.OpenIdAuthController
import com.unblu.brandeableagentapp.model.*
import com.unblu.brandeableagentapp.nav.NavGraph
import com.unblu.brandeableagentapp.ui.theme.BrandeableAgentAppTheme
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplicationHelper
import com.unblu.sdk.core.errortype.UnbluClientErrorType
import com.unblu.sdk.core.internal.utils.Logger
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import net.openid.appauth.*
import java.util.*


class MainActivity : ComponentActivity() {
    private val compositeDisposable = CompositeDisposable()
    private val unbluController: UnbluController
        get() = (application as AgentApplication).getUnbluController()
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var unbluScreenViewModel: UnbluScreenViewModel
    private lateinit var viewModelProvider : ViewModelProvider

    /**
     *  you should only keep this property if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
     */
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    /**
     *  you should only keep this property if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
     */
    private lateinit var openIdAuthController: OpenIdAuthController

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        configViewModels()
        setContent {
            val navController = rememberAnimatedNavController()
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }

            BrandeableAgentAppTheme {
                NavGraph(
                    navController,
                    viewModelProvider
                )
            }
        }
        compositeDisposable.addAll(
            Unblu
                .onAgentInitialized()
                .subscribe(
                    { agentClient ->
                        unbluScreenViewModel.setClient(agentClient)
                        compositeDisposable.add(agentClient.openConversation.subscribe { conversation->
                            unbluScreenViewModel.emitChatOpen(conversation.isPresent)
                        })
                    },
                    { error -> Log.e("MainActivity", "Error: ${error.localizedMessage}") }
                ),
            Unblu
                .onUiHideRequest()
                .subscribe(
                    {
                        unbluScreenViewModel.setShowDialog(true)
                    },
                    { error -> Log.e("MainActivity", "Error: ${error.localizedMessage}") }),
            Unblu.onError().subscribe { errorData->
                Log.e("MainActivity", "session ended: ${errorData.message}")
                if ((errorData.errorType == UnbluClientErrorType.AUTHENTICATION) && errorData.message.contains("Forbidden")
                    || errorData.errorType == UnbluClientErrorType.AUTHORIZATION
                    || (errorData.errorType == UnbluClientErrorType.INVALID_URL)
                    || (errorData.errorType == UnbluClientErrorType.INTERNAL)){
                    loginViewModel.setErrorMessage(errorData.message)
                    loginViewModel.resetSSOLogin()
                }
                unbluScreenViewModel.endSession()
            }
        )

        /**
         *  you should only keep this method if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
         */
        configureOAuth()

        /**
         *  delete the block below if the [AuthenticationType] is [AuthenticationType.Direct]
         */
        if(unbluController.getHasUiShowRequestValueAndReset()){
            /**
             *  you should edit this code according to the [AuthenticationType]
             *  [AuthenticationType.OAuth] : keep 'openIdAuthController.startSignIn(signInLauncher)'
             *  [AuthenticationType.WebProxy] : keep 'loginViewModel.launchSSO()'
             */
            when(AppConfiguration.authType){
                AuthenticationType.OAuth -> openIdAuthController.startSignIn(signInLauncher)
                AuthenticationType.WebProxy -> loginViewModel.launchSSO()
                else-> {
                        Logger.d("MainActivity", "Ui was requested, but direct login is required")
                }
            }
        }
    }

    private fun configViewModels() {
        viewModelProvider = ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )
        loginViewModel = viewModelProvider[LoginViewModel::class.java]
        loginViewModel.setUnbluController(unbluController)
        unbluScreenViewModel = viewModelProvider[UnbluScreenViewModel::class.java]

        val settingsViewModel = viewModelProvider[SettingsViewModel::class.java]
        settingsViewModel.fetchSettingsModel((application as AgentApplication).getUnbluPrefs())
    }

    /**
     *  you should only keep this function if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
     */
    private fun configureOAuth() {
            openIdAuthController = OpenIdAuthController(application as AgentApplication)
            configSignIn()
            lifecycleScope.launch {
                openIdAuthController.eventReceived.collect { event ->
                    if(AppConfiguration.authType != AuthenticationType.OAuth) return@collect
                    when (event) {
                        is TokenEvent.TokenReceived -> {
                            Log.d(MainActivity::javaClass.name, "Got token: ${event.token}")
                            // Handle token received
                            unbluController.setOAuthToken(event.token)
                            if(unbluController.getClient() == null)
                                loginViewModel.startUnblu(null)
                        }
                        is TokenEvent.ErrorReceived -> {
                            // Handle error received
                            Log.e(MainActivity::javaClass.name, "Failed to receive token, will reset")
                            loginViewModel.resetSSOLogin()
                        }
                    }
                }
            }

            Log.d(MainActivity::javaClass.name, "Will register for activity result")
            lifecycleScope.launch {
                loginViewModel.customTabsOpen.collect { open ->
                    if(open) {
                        openIdAuthController.startSignIn(signInLauncher)
                    }
                }
            }

    }

    /**
     *  you should only keep this function if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
     */
    private fun configSignIn() {
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            this::handleSignInResult
        )
    }

    /**
     *  you should only keep this function if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
     */
    private fun handleSignInResult(result: ActivityResult) {
        result.data
            ?.apply { openIdAuthController.handleActivityResult(result.resultCode, this) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent")
        intent.apply {
            UnbluApplicationHelper.onNewIntent(this?.extras)
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}