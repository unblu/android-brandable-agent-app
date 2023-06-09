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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.LoginState
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.NavigationState
import com.unblu.brandeableagentapp.nav.NavRoute
import com.unblu.brandeableagentapp.ui.theme.Rubik

/**
 *  This class is used in case the login type scenario is [AuthenticationType.Direct], if not you can delete this class and references all together
 * @param navController NavHostController
 * @param viewModel LoginViewModel
 */

@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    val username by remember { viewModel.username }
    val password by remember { viewModel.password }
    val snackbarHostState = remember { SnackbarHostState() }
    val loginState by viewModel.loginState.collectAsState()
    val passwordVisibility by viewModel.passwordVisiblity.collectAsState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val inputAndButtonHeight = max(56.dp,screenHeight * 0.06f)
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = CenterHorizontally
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
                                .padding(top = 8.dp),
                            style = TextStyle(
                                fontFamily = Rubik,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                            )
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
                                        .border(
                                            1.dp,
                                            buttonBorderColor,
                                            shape = RoundedCornerShape(4.dp)
                                        )
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
                                        .border(
                                            1.dp,
                                            buttonBorderColor,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Text(stringResource(R.string.login_button))
                                }
                            }
                        }
                    }

                }
            }
            SnackbarHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                hostState = snackbarHostState
            )
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


@Preview(
    name = "Samsung Galaxy A51 Preview",
    widthDp = 392,  // approximate width in dp (1080px / 2.75)
    heightDp = 873  // approximate height in dp (2400px / 2.75)
)
@Preview(
    name = "Samsung Galaxy S20 Ultra Preview",
    widthDp = 392,  // approximate width in dp (1440px / 3.5)
    heightDp = 914  // approximate height in dp (3200px / 3.5)
)
@Preview(
    name = "Xiaomi Mi 10 Pro Preview",
    widthDp = 394,  // approximate width in dp (1080px / 2.75)
    heightDp = 853  // approximate height in dp (2340px / 2.75)
)
@Preview(
    name = "Oppo Find X2 Pro Preview",
    widthDp = 416,  // approximate width in dp (1440px / 3.5)
    heightDp = 906  // approximate height in dp (3168px / 3.5)
)
@Preview(
    name = "OnePlus 8 Pro Preview",
    widthDp = 416,  // approximate width in dp (1440px / 3.5)
    heightDp = 906  // approximate height in dp (3168px / 3.5)
)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    val viewModel = LoginViewModel()
    Surface {
        LoginScreen(navController = navController, viewModel = viewModel)
    }
}
