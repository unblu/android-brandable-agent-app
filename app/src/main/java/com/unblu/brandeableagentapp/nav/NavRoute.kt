package com.unblu.brandeableagentapp.nav

sealed class NavRoute(var route: String) {
    object Splash: NavRoute("splash")
    object Login: NavRoute("login")
    object Unblu: NavRoute("success")
    object Settings: NavRoute("settings")
}
