package com.unblu.brandeableagentapp.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.sdk.core.Unblu

@SuppressLint("ServiceCast")
@Composable
fun SuccessScreen(
    navController: NavController,
    viewModel: UnbluScreenViewModel,
    onNavigateBack : ()->Unit
) {
    // State for showing the logout confirmation dialog
    val showDialog by viewModel.showDialog
    val unbluView by viewModel.mainView

    AndroidView(modifier = Modifier.fillMaxSize(), factory = {
        unbluView?.detachView()
        unbluView?: RelativeLayout(it)
    })

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowDialog(false) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    onNavigateBack()
                    unbluView?.detachView()
                    navController.navigate("login")
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDialog(false) }) {
                    Text("No")
                }
            }
        )
    }

}

private fun View.detachView() {
    val uiParent = this.parent as ViewGroup?
    uiParent?.removeView(this)
}