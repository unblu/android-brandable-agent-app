package com.unblu.brandeableagentapp.model

import android.view.View
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UnbluScreenViewModel : ViewModel() {
    private var _showDialog  = mutableStateOf(false)
    val showDialog  : State<Boolean> get() = _showDialog
    private var _mainView  = mutableStateOf<View?>(null)
    val mainView : State<View?>get() = _mainView
    private val _sessionEnded = MutableSharedFlow<Unit>()
    val sessionEnded = _sessionEnded.asSharedFlow()

    fun setMainView(mainView: View?) {
        _mainView.value =  mainView
    }

    fun setShowDialog(show: Boolean) {
        _showDialog.value = show
    }

    fun endSession(){
        viewModelScope.launch {
            _sessionEnded.emit(Unit)
        }
    }
}