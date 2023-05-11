package com.unblu.brandeableagentapp.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.model.authTypeFromName

@SuppressLint("ServiceCast")
@Composable
fun SettingsScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SettingsView",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(16.dp)
        )

        RadioButtonGroup(options = listOf(AuthenticationType.Direct.toString(), AuthenticationType.WebProxy.toString(),AuthenticationType.OAuth.toString())){
            AppConfiguration.authType = authTypeFromName(it)
        }
    }
    navController.navigate("login")
}

@Composable
fun RadioButtonGroup(options: List<String>, onOptionSelected: (String) -> Unit) {
    var selectedOption by remember { mutableStateOf(options[0]) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = option == selectedOption,
                    onClick = {
                        selectedOption = option
                        onOptionSelected(option)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}



