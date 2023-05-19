package com.unblu.brandeableagentapp.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.model.LoginState
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.NavigationState
import com.unblu.brandeableagentapp.nav.NavRoute


@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    val username by remember { viewModel.username }
    val password by remember { viewModel.password }
    val snackbarHostState = remember { SnackbarHostState() }
    val loginState by viewModel.loginState.collectAsState()
    val passwordVisibility by viewModel.passwordVisiblity.collectAsState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val inputAndButtonHeight = screenHeight * 0.07f
    val backgroundColor = colorResource(id = R.color.login_screen_background)
    val logoColor = colorResource(id = R.color.logo_color)
    val inputBackground = colorResource(id = R.color.input_background_color)
    val borderColor = colorResource(id = R.color.input_border_color)
    val buttonBackgroundColor = colorResource(id = R.color.login_button_background)
    val buttonTextColor = colorResource(id = R.color.login_button_text)
    val inputTextColor = colorResource(id = R.color.login_input_text)
    val progressIndicatorColor = colorResource(id = R.color.progress_color)
    val buttonBorderColor = colorResource(id = R.color.login_button_border)

    Surface(color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f))
             {
                Column(Modifier.align(Center)){
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        colorFilter = ColorFilter.tint(logoColor),
                        contentDescription = null,
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .size(128.dp)
                            .onDoubleClick {
                                navController.navigate(NavRoute.Settings.route)
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

            Column(Modifier.weight(3f)) {
                // Username
                LabeledTextField(
                    label = stringResource(R.string.login_username_label),
                    value = username,
                    onValueChange = viewModel::onUsernameChange,
                    inputHeight = inputAndButtonHeight,
                    inputBackground = inputBackground,
                    borderColor = borderColor,
                    inputTextColor = inputTextColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password
                LabeledTextField(
                    label = stringResource(R.string.login_password_label),
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    inputHeight = inputAndButtonHeight,
                    inputBackground = inputBackground,
                    borderColor = borderColor,
                    inputTextColor = inputTextColor,
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.setPasswordVisiblity(!passwordVisibility) }) {
                            Icon(
                                imageVector = if (passwordVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Center
                    ) {
                        when (loginState) {
                            LoginState.LoggingIn -> {
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = buttonBackgroundColor,
                                        contentColor = buttonTextColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(inputAndButtonHeight)
                                        .border(1.dp, buttonBorderColor, shape = RoundedCornerShape(4.dp))
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
                                    onClick = { viewModel.login(username, password) },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = buttonBackgroundColor,
                                        contentColor = buttonTextColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(inputAndButtonHeight)
                                        .border(1.dp, buttonBorderColor, shape = RoundedCornerShape(4.dp))
                                ) {
                                    Text(stringResource(R.string.login_button))
                                }
                            }
                        }
                    }
                    SnackbarHost(snackbarHostState)
                }
            }
    }
    LaunchedEffect(viewModel.navigationState) {
        viewModel.navigationState.collect { state ->
            when (state) {
                is NavigationState.Success -> {
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
                    snackbarHostState.showSnackbar("Login failed: ${state.message}")
                }
                else -> {
                    Log.d("LoginScreen", "Nav State null")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    val viewModel = LoginViewModel()
    Surface {
        LoginScreen(navController = navController, viewModel = viewModel)
    }
}
