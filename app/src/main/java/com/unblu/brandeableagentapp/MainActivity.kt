package com.unblu.brandeableagentapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.unblu.brandeableagentapp.api.UnbluController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.sso.oauth.OpenIdAuthController
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.TokenEvent
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.brandeableagentapp.nav.NavGraph
import com.unblu.brandeableagentapp.ui.theme.BrandeableAgentAppTheme
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplicationHelper
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.errortype.UnbluClientErrorType
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import net.openid.appauth.*
import java.util.*


class MainActivity : ComponentActivity() {
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    val compositeDisposable = CompositeDisposable()
    private lateinit var openIdAuthController: OpenIdAuthController

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelStore = ViewModelStore()
        val unbluController = (application as AgentApplication).unbluController
        val loginViewModel = ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[LoginViewModel::class.java]
        loginViewModel.setUnbluController(unbluController)
        val unbluScreenViewModel = ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[UnbluScreenViewModel::class.java]

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
                    ViewModelProvider(
                        viewModelStore,
                        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                    )
                )
            }
        }

        compositeDisposable.addAll(
            Unblu
                .onAgentInitialized()
                .subscribe(
                    { agentClient ->
                        unbluScreenViewModel.setMainView(agentClient.mainView)
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
                Log.w("MainActivity", "session ended: ${errorData.message}")
                if ((errorData.errorType == UnbluClientErrorType.AUTHENTICATION) && errorData.message.contains("Forbidden")
                    || errorData.errorType == UnbluClientErrorType.AUTHORIZATION
                    || (errorData.errorType == UnbluClientErrorType.INVALID_URL)
                    || (errorData.errorType == UnbluClientErrorType.INTERNAL)){
                     unbluScreenViewModel.endSession()
                }
            }
        )
        configureAuthType(loginViewModel, (application as AgentApplication).getUnbluPrefs(), (application as AgentApplication).unbluController)
    }

    private fun configureAuthType(
        loginViewModel: LoginViewModel,
        unbluPreferencesStorage: UnbluPreferencesStorage,
        unbluController: UnbluController
    ) {
        if (AppConfiguration.authType is AuthenticationType.OAuth) {
            openIdAuthController = OpenIdAuthController(this, unbluPreferencesStorage)
            lifecycleScope.launch {
                openIdAuthController.eventReceived.collect { event ->
                    when (event) {
                        is TokenEvent.TokenReceived -> {
                            Log.w(MainActivity::javaClass.name, "Got token: ${event.token}")
                            // Handle token received
                            unbluController.setAccessToken(event.token)
                            loginViewModel.startUnblu(null)
                        }
                        is TokenEvent.ErrorReceived -> {
                            // Handle error received
                            Log.e(MainActivity::javaClass.name, "Got token: ${event.error}")
                            loginViewModel.resetSSOLogin()
                        }
                    }
                }
            }

            Log.w(MainActivity::javaClass.name, "Will register for activity result")
            signInLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                this::handleSignInResult
            )
            lifecycleScope.launch {
                loginViewModel.customTabsOpen.collect { open ->
                    if(open)
                        openIdAuthController.startSignIn(signInLauncher)
                }
            }
        }
    }

    private fun handleSignInResult(result: ActivityResult) {
        result.data
            ?.apply { openIdAuthController.handleActivityResult(result.resultCode, this) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.w("MainActivity", "onNewIntent")
        intent.apply {
            UnbluApplicationHelper.onNewIntent(this?.extras)
        }
    }

    override fun onDestroy() {
        viewModelStore.clear()
        super.onDestroy()
    }

}