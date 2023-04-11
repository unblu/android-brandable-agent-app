package com.unblu.brandeableagentapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import com.unblu.brandeableagentapp.model.LoginViewModel
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.model.NavigationState

@Composable
fun LoginScreenSSO(navController: NavHostController, viewModel: LoginViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    Surface(color = colorResource(id = R.color.login_screen_background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text
            Text(stringResource(R.string.login_screen_sso_text))

            Spacer(modifier = Modifier.height(16.dp))

            // Button
            Button(
                onClick = { viewModel.loginSSO() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.login_screen_sso_button_background),
                    contentColor = colorResource(id = R.color.login_screen_sso_button_text)
                )
            ) {
                Text(stringResource(R.string.login_screen_sso_button))
            }
        }
    }

    LaunchedEffect(viewModel.navigationState) {
        viewModel.navigationState.collect { state ->
            when (state) {
                is NavigationState.Success -> {
                    // Navigate to success screen
                }
                is NavigationState.Failure-> {
                    snackbarHostState.showSnackbar("Login failed: ${state.message}")
                }
                else -> {
                    Log.d("LoginScreenSSO", "Nav State null")
                }
            }
        }
    }
}


