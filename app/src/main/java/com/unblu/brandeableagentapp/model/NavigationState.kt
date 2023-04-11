package com.unblu.brandeableagentapp.model

import android.view.View

sealed class NavigationState {
    data class Success(val view : View) : NavigationState()
    data class Failure(val message: String) : NavigationState()
}

