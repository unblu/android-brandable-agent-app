package com.unblu.brandeableagentapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.sso.NoCacheConnectionBuilder
import com.unblu.brandeableagentapp.login.sso.model.IdentityProvider
import com.unblu.brandeableagentapp.login.sso.model.IdentityProviderType
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
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
    val compositeDisposable = CompositeDisposable()
    private lateinit var authService: AuthorizationService
    private lateinit var authActivityResultLauncher: ActivityResultLauncher<Intent>

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
                if ((errorData.errorType == UnbluClientErrorType.AUTHENTICATION) && errorData.message == "Forbidden"
                    || errorData.errorType == UnbluClientErrorType.AUTHORIZATION
                    || (errorData.errorType == UnbluClientErrorType.INVALID_URL && errorData.message == "Blocked")) {
                     unbluScreenViewModel.endSession()
                }
            }
        )

        if (AppConfiguration.authType is AppConfiguration.AuthenticationType.OAuth) {
            authService = AuthorizationService(this, AppAuthConfiguration.Builder()
                .setConnectionBuilder(NoCacheConnectionBuilder.INSTANCE)
                .build()
            )

            Log.w(MainActivity::javaClass.name, "Will register for activity result")
            authActivityResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    Log.w(MainActivity::javaClass.name, "RESULT CALLBACK")
                    loginViewModel.resetSSOLogin()
                    val data = result.data
                    if (data != null) {
                        val response = AuthorizationResponse.fromIntent(data)
                        val error = AuthorizationException.fromIntent(data)
                        if (response != null) {
                            // Successfully authenticated, handle the response here
                            val authState = AuthState(response, error)
                            Log.w(MainActivity::javaClass.name, "SUCESSSSSSSSSS")
                            // Save the authState or use it to get tokens
                        } else {
                            // Authentication failed, handle the error here
                            Log.e("MainActivity", "Auth failed: $error")
                        }
                    }
                }

            val identityProvider =
                AppConfiguration.authProviders[AppConfiguration.authProvider.takeIf { it is IdentityProviderType.Microsoft }
                    ?: IdentityProviderType.Keycloak]
            identityProvider?.let { identityProvider ->
                lifecycleScope.launch {
                    loginViewModel.customTabsOpen.collect { open ->
                        if(open)
                            startAuthProcess(identityProvider)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.w("MainActivity", "onNewIntent")
        intent?.apply {
            UnbluApplicationHelper.onNewIntent(this.extras)
        }
    }

    override fun onDestroy() {
        viewModelStore.clear()
        super.onDestroy()
    }

    private fun startAuthProcess(identityProvider: IdentityProvider) {
        val authorizationUrl = getAuthUrl(identityProvider)
        val builder = CustomTabsIntent.Builder()
            .setShareState(SHARE_STATE_OFF)
            .setStartAnimations(this, R.anim.slide_up, R.anim.slide_down)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(authorizationUrl))
    }

    fun getAuthUrl(provider: IdentityProvider): String {
        var url = provider.webAuthServerAddress
        if (provider.type == IdentityProviderType.Microsoft && provider.webAuthTenant != null) {
            url += "/${provider.webAuthTenant}"
        }
        url += provider.webAuthBaseUrl + provider.webAuthGetTokenId
        return url
    }

}