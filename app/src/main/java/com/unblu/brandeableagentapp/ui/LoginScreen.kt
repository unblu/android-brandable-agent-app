package com.unblu.brandeableagentapp.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.model.LoginState
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.NavigationState


@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {
    val (username, onUsernameChange) = remember { mutableStateOf("superadmin") }
    val (password, onPasswordChange) = remember { mutableStateOf("harmless-squire-spotter") }
    val snackbarHostState = remember { SnackbarHostState() }
    val loginState by viewModel.loginState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") })
            TextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") })
            when (loginState) {
                LoginState.LoggingIn -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {
                    Button(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = { viewModel.login(username, password) }) {
                        Text("Login")
                    }
                }
            }
            SnackbarHost(snackbarHostState)
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
