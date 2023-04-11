package com.unblu.brandeableagentapp.ui

import android.util.Log
import androidx.compose.runtime.Composable
import com.unblu.brandeableagentapp.model.LoginViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.unblu.brandeableagentapp.model.NavigationState

@Composable
fun LoginScreenSSO(navController: NavHostController, viewModel: LoginViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    Column {
        Text("Single Sign-On")
        Button(onClick = {
            viewModel.loginSSO()
        }) {
            Text("Login with SSO")
        }

        SnackbarHost(snackbarHostState)
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