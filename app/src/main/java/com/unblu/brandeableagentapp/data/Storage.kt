package com.unblu.brandeableagentapp.data

import com.google.gson.Gson
import com.unblu.brandeableagentapp.model.SettingsModel
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage

object Storage {
    const val  UNBLU_USERNAME = "u_username"
    const val  UNBLU_SETTINGS ="u_settings"

    fun getUnbluSettings(unbluPreferencesStorage: UnbluPreferencesStorage) : SettingsModel {
        return  try {
            val storedSettingsJson = unbluPreferencesStorage.get(UNBLU_SETTINGS)
            storedSettingsJson?.let { settingsJsons->
                Gson().fromJson(settingsJsons, SettingsModel::class.java)
            }?: SettingsModel()
        }catch (e: Exception){
            SettingsModel()
        }
    }
}