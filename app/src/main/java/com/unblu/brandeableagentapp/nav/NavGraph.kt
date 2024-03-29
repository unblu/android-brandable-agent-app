package com.unblu.brandeableagentapp.nav

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.SettingsViewModel
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.brandeableagentapp.ui.*

@SuppressLint("ServiceCast")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(navController: NavHostController, viewModelStore: ViewModelProvider) {
    val loginViewModel = viewModelStore[LoginViewModel::class.java]
    val successViewModel = viewModelStore[UnbluScreenViewModel::class.java]

    navController.enableOnBackPressed(false)

    AnimatedNavHost(navController, startDestination = NavRoute.Splash.route) {
        composable(
            NavRoute.Splash.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            SplashScreen(navController)
        }

        //This screen is useful for development purposes, should be deleted when no longer needed
        composable(
            route = NavRoute.Settings.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            val settingsViewModel = viewModelStore[SettingsViewModel::class.java]
            loginViewModel.resetSSOLogin()
            SettingsScreen(navController, settingsViewModel.settingsModel,
                {
                    settingsViewModel.saveModel()
                }) { updatedModel ->
                settingsViewModel.updateSettingsModel(updatedModel)
            }
        }

        composable(
            route = NavRoute.Login.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            //Edit this according to the AuthenticationType you will be using, if AuthenticationType.Direct just keep "LoginScreen(navController, loginViewModel)", if not keep LoginScreenSSO(navController, loginViewModel) and delete the other lines
            if (AppConfiguration.authType !is AuthenticationType.Direct) {
                LoginScreenSSO(navController, loginViewModel)
            } else {
                LoginScreen(navController, loginViewModel)
            }
        }

        composable(
            route = NavRoute.Unblu.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            SuccessScreen(navController, successViewModel) {
                loginViewModel.stopUnblu()
                successViewModel.setShowDialog(false)
            }
        }
    }
}
