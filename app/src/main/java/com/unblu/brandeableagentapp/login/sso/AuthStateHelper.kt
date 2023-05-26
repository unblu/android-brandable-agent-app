package com.unblu.brandeableagentapp.login.sso

import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import net.openid.appauth.AuthState
import org.json.JSONException


const val AUTH_STATE = "auth_state"

/**
 *  This file can be deleted in case the selected [AuthenticationType] is not [AuthenticationType.OAuth].
 * @param unbluPreferencesStorage UnbluPreferencesStorage
 * @return AuthState
 */
fun getAuthState(unbluPreferencesStorage: UnbluPreferencesStorage) : AuthState {
    return when(val stateData = unbluPreferencesStorage.get(AUTH_STATE)){
        null -> AuthState()
        else-> try {
            AuthState.jsonDeserialize(stateData)
        } catch (e: JSONException) {
            AuthState()
        }
    }
}

fun storeAuthState(authState: AuthState, storage: UnbluPreferencesStorage){
    storage.put(AUTH_STATE, authState.jsonSerializeString())
}
