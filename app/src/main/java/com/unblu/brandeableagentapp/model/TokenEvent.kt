package com.unblu.brandeableagentapp.model

sealed class TokenEvent {
        data class TokenReceived(val token: String) : TokenEvent()
        data class ErrorReceived(val error: String) : TokenEvent()
    }