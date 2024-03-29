package com.unblu.brandeableagentapp.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.data.Storage.UNBLU_SETTINGS
import com.unblu.brandeableagentapp.login.sso.deleteAuthState
import com.unblu.brandeableagentapp.util.AuthenticationTypeAdapter
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage

class SettingsViewModel : ViewModel() {
    private lateinit var unbluPreferencesStorage: UnbluPreferencesStorage
    private val _settingsModel = mutableStateOf(SettingsModel())
    val settingsModel: State<SettingsModel> get() = _settingsModel

    fun fetchSettingsModel(unbluPreferencesStorage : UnbluPreferencesStorage) {
        this.unbluPreferencesStorage = unbluPreferencesStorage
        val storedSettingsJson = unbluPreferencesStorage.get(UNBLU_SETTINGS)
        val gson  = GsonBuilder()
            .registerTypeAdapter(AuthenticationType::class.java, AuthenticationTypeAdapter())
            .create()
        _settingsModel.value = storedSettingsJson?.let {
            gson.fromJson(it, SettingsModel::class.java)
        }?: SettingsModel()
        updateSettingsModel(_settingsModel.value)
    }

    fun updateSettingsModel(updatedModel: SettingsModel) {
        _settingsModel.value = updatedModel
    }

    fun saveModel() {
        if(_settingsModel.value.authType != AuthenticationType.OAuth)
            deleteAuthState(unbluPreferencesStorage)
        unbluPreferencesStorage.put(UNBLU_SETTINGS, Gson().toJson(_settingsModel.value))
        AppConfiguration.updateFromSettingsModel(_settingsModel.value)
    }

}