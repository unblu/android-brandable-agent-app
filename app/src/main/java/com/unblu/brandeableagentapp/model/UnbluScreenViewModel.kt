package com.unblu.brandeableagentapp.model

import android.view.View
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unblu.sdk.core.agent.UnbluAgentClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UnbluScreenViewModel : ViewModel() {
    private var _showDialog  = mutableStateOf(false)
    val showDialog  : State<Boolean> get() = _showDialog
    private val _sessionEnded = MutableSharedFlow<Unit>()
    val sessionEnded = _sessionEnded.asSharedFlow()
    private var _chatUiOpen =  mutableStateOf(false)
    val chatUiOpen: State<Boolean> = _chatUiOpen
  private  lateinit var agentClient: UnbluAgentClient

    fun setClient(agentClient: UnbluAgentClient) {
        this.agentClient =  agentClient
    }

    fun setShowDialog(show: Boolean) {
        _showDialog.value = show
    }

    fun endSession(){
        viewModelScope.launch {
            _sessionEnded.emit(Unit)
        }
    }

    fun emitChatOpen(chatUiOpen: Boolean) {
            _chatUiOpen.value = chatUiOpen
    }

    fun getView() : View{
        return agentClient.mainView
    }
}