package com.unblu.brandeableagentapp.model

import android.view.View
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UnbluScreenViewModel : ViewModel() {
    private var _showDialog  = mutableStateOf(false)
    val showDialog  : State<Boolean> get() = _showDialog
    private var _mainView  = mutableStateOf<View?>(null)
    val mainView : State<View?>get() = _mainView

    fun setMainView(mainView: View?) {
        _mainView.value =  mainView
    }

    fun setShowDialog(show: Boolean) {
        _showDialog.value = show
    }
}