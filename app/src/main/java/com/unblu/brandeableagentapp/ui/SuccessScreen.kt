package com.unblu.brandeableagentapp.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.sdk.core.ui.UnbluNavUtil

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

    AndroidView(modifier = Modifier
        .fillMaxSize()
        .focusable(true)
        , factory = {
        unbluView?.detachView()
        unbluView?.apply { configView (this) }
        unbluView?: RelativeLayout(it)
    })

    if (showDialog) {
        CustomAlertDialog(
            onCancel = { viewModel.setShowDialog(false) },
            title =  stringResource(id = R.string.logout_confirmation_title),
            message = stringResource(id = R.string.logout_confirmation_message),
            cancelText = stringResource(id = R.string.logout_confirmation_negative),
            confirmText = stringResource(id = R.string.logout_confirmation_positive),
             onConfirm = {
                    goBack(onNavigateBack,unbluView,navController)
                },
            alertDialogColors = AlertDialogColorDefaults.alertDialogColors()
        )
    }

    BackHandler() {
        unbluView?.let {
            UnbluNavUtil.getUnbluNav(unbluView)?.goBack { didGoBack->
                if(!didGoBack){
                    viewModel.setShowDialog(true)
                }
            }
        } ?: run {
            goBack(onNavigateBack, unbluView, navController)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.sessionEnded.collect {
            goBack(onNavigateBack,unbluView,navController)
        }
    }

}

private fun goBack(
    onNavigateBack: () -> Unit,
    unbluView: View?,
    navController: NavController
) {
    Log.w("SuccessScreen", "will go back")
    onNavigateBack()
    unbluView?.detachView()
    navController.navigate("login")
}

fun configView(unbluView: View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        unbluView.isFocusable = true
    }
    unbluView.isFocusableInTouchMode = true
    unbluView.requestFocus()
}

private fun View.detachView() {
    val uiParent = this.parent as ViewGroup?
    uiParent?.removeView(this)
}
