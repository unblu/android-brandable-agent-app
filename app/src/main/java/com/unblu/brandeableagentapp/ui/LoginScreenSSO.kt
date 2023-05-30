package com.unblu.brandeableagentapp.ui

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.login.proxy.ProxyWebViewClient
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.LoginState
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.NavigationState
import com.unblu.brandeableagentapp.util.CookieUtil

/**
 *  This class is used in case the login type scenario is [AuthenticationType.WebProxy] or [AuthenticationType.OAuth] , if not you can delete this class and references all together
 * @param navController NavHostController
 * @param viewModel LoginViewModel
 */
@Composable
fun LoginScreenSSO(navController: NavHostController, viewModel: LoginViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val backgroundColor = colorResource(id = R.color.login_screen_background)
    val showWebview by viewModel.showWebview.collectAsState()
    val toolbarColor = colorResource(id = R.color.login_sso_toolbar_background)


    Box(modifier = Modifier.fillMaxSize()) {
        // Login UI
        Surface(color = backgroundColor) {
            LoginUI(viewModel, navController)

            /**
             *  You can delete this [AnimatedVisibility] and its children if the [AuthenticationType] is [AuthenticationType.OAuth]
             */

            AnimatedVisibility(
                visible = showWebview,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {},
                        backgroundColor = toolbarColor,
                        navigationIcon = {
                            IconButton(onClick = { viewModel.resetSSOLogin() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    )

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            val webView = WebView(context).apply {
                                webViewClient = ProxyWebViewClient(viewModel.onCookieReceived)
                            }
                            CookieUtil.clear {
                                webView.clearCache(true)
                                webView.loadUrl(AppConfiguration.webAuthProxyServerAddress)
                            }
                            webView
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(viewModel.navigationState) {
        viewModel.navigationState.collect { state ->
            when (state) {
                is NavigationState.Success -> {
                    Log.d("LoginScreenSSO", "NavigationState.Success")
                    viewModel.resetSSOLogin()
                    navController.navigate(
                        "success", navigatorExtras = ActivityNavigatorExtras(
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                navController.context as Activity,
                                state.view,
                                "unblu"
                            )
                        )
                    )
                }
                is NavigationState.Failure -> {
                    Log.e("LoginScreenSSO", "NavigationState.Failure: ${state.message}")
                    snackbarHostState.showSnackbar("Login failed: ${state.message}")
                }
                else -> {
                    Log.d("LoginScreenSSO", "Nav State null")
                }
            }
        }
    }
}

@Composable
fun LoginUI(
    viewModel: LoginViewModel,
    navController: NavHostController
) {
    val loginState by viewModel.loginState.collectAsState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val buttonHeight = screenHeight * 0.060f
    val progressIndicatorColor = colorResource(id = R.color.progress_color)
    val buttonBorderColor = colorResource(id = R.color.login_button_border)
    val logoColor = colorResource(id = R.color.logo_color)
    val buttonBackgroundColor = colorResource(id = R.color.login_screen_sso_button_background)
    val buttonTextColor = colorResource(id = R.color.login_screen_sso_button_text)
    val backgroundColor = colorResource(id = R.color.login_screen_background)

    Column(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            Column(Modifier.align(Center)) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    colorFilter = ColorFilter.tint(logoColor),
                    contentDescription = null,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .size(128.dp)
                        .onDoubleClick {
                            navController.navigate("settings")
                        }
                )

                // Subtitle
                Text(
                    stringResource(R.string.login_subtitle),
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }
        }

        Column(Modifier.weight(2f)) {
            // Login Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (loginState) {
                    LoginState.LoggingIn -> {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = buttonBackgroundColor,
                                contentColor = buttonTextColor
                            ),
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = progressIndicatorColor
                            )
                            Text(stringResource(R.string.login_button_logging_in))
                        }
                    }
                    else -> {
                        Button(
                            onClick = { viewModel.launchSSO() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = buttonBackgroundColor,
                                contentColor = buttonTextColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .border(1.dp, buttonBorderColor, shape = RoundedCornerShape(4.dp))
                        ) {
                            Text(stringResource(R.string.login_screen_sso_text))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenSSOPreview() {
    val dummyNavHostController = rememberNavController()
    val dummyViewModel = LoginViewModel()

    MaterialTheme {
        LoginScreenSSO(
            navController = dummyNavHostController,
            viewModel = dummyViewModel
        )
    }
}
