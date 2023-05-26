package com.unblu.brandeableagentapp.model

/**
 *  you should only keep this class if the selected [AuthenticationType] is  [AuthenticationType.OAuth]
 */
sealed class TokenEvent {
        data class TokenReceived(val token: String) : TokenEvent()
        data class ErrorReceived(val error: String) : TokenEvent()
    }