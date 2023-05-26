package com.unblu.brandeableagentapp.nav
import com.unblu.brandeableagentapp.ui.SettingsScreen

sealed class NavRoute(var route: String) {
    object Splash: NavRoute("splash")
    object Login: NavRoute("login")
    object Unblu: NavRoute("success")

    /**
     *  No need for this Route if you delete [SettingsScreen]
     */
    object Settings: NavRoute("settings")
}
