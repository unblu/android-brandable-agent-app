package com.unblu.brandeableagentapp.nav

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.unblu.brandeableagentapp.BuildConfig
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.brandeableagentapp.ui.*

@SuppressLint("ServiceCast")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(navController: NavHostController, viewModelStore: ViewModelProvider) {
    val loginViewModel = viewModelStore[LoginViewModel::class.java]
    val successViewModel = viewModelStore[UnbluScreenViewModel::class.java]

    navController.enableOnBackPressed(false)

    AnimatedNavHost(navController, startDestination = "splash") {
        composable("splash", enterTransition = {  fadeIn() }, exitTransition = {  fadeOut() }) {
            SplashScreen(navController)
        }
        composable("login", enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
            if (AppConfiguration.authType !is AppConfiguration.AuthenticationType.Direct) {
                LoginScreenSSO(navController, loginViewModel)
            } else {
                LoginScreen(navController, loginViewModel)
            }
        }
        composable("success", enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
            SuccessScreen(navController, successViewModel){
                loginViewModel.stopUnblu()
                successViewModel.setShowDialog(false)
            }
        }
    }
}
