package com.unblu.brandeableagentapp.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.SettingsModel
import com.unblu.brandeableagentapp.model.authTypeFromName
import com.unblu.brandeableagentapp.nav.NavRoute

@SuppressLint("ServiceCast")
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsModel: State<SettingsModel>,
    updateSettingsModel: (SettingsModel) -> Unit,
) {
    val settings by remember { settingsModel }
    val toolbarColor = colorResource(id = R.color.login_sso_toolbar_background)

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "SettingsView"
                )
            },
            backgroundColor = toolbarColor,
            navigationIcon = {
                IconButton(onClick = { navController.navigate(NavRoute.Login.route) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        )
    }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(horizontal = 4.dp, vertical = it.calculateTopPadding())),
            horizontalAlignment = Alignment.Start
        ) {
            settings.let { model ->
                item {
                    RadioButtonGroup(
                        options = listOf(
                            AuthenticationType.Direct.name,
                            AuthenticationType.WebProxy.name,
                            AuthenticationType.OAuth.name
                        ),
                        onOptionSelected = { authType ->
                            settings.let { model->
                                Log.w("SettingsScreen ", "auth type: $authType" )
                                val updatedModel = model.copy(authType = authTypeFromName(authType))
                                updateSettingsModel(updatedModel)
                            }
                        },
                        defaultOption = settings.authType
                    )
                }
                item {
                    SettingsTextField(
                        label = "Server URL",
                        value = model.unbluServerUrl,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(unbluServerUrl = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "API Key",
                        value = model.unbluApiKey,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(unbluApiKey = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "Entry Path",
                        value = model.entryPath,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(entryPath = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "Web Auth Proxy Server Address",
                        value = model.webAuthProxyServerAddress,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(webAuthProxyServerAddress = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "OAuth Client ID",
                        value = model.oAuthClientId,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(oAuthClientId = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "OAuth Redirect URI",
                        value = model.oAuthRedirectUri,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(oAuthRedirectUri = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "OAuth Endpoint",
                        value = model.oAuthEndpoint,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(oAuthEndpoint = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    SettingsTextField(
                        label = "OAuth Token Endpoint",
                        value = model.oAuthTokenEndpoint,
                        onValueChange = { newValue ->
                            val updatedModel = model.copy(oAuthTokenEndpoint = newValue)
                            updateSettingsModel(updatedModel)
                        }
                    )
                }
                item {
                    Button(
                        onClick = { navController.navigate(NavRoute.Login.route) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun SettingsScreenPreview() {
    val navController = rememberNavController()
    val settingsModel = remember { mutableStateOf(SettingsModel()) }
    val updateSettingsModel: (SettingsModel) -> Unit = {}

    Surface {
        SettingsScreen(navController, settingsModel, updateSettingsModel)
    }
}