package com.unblu.brandeableagentapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Implement your splash screen UI here

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("login")
    }
}