package com.unblu.brandeableagentapp.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.sdk.core.ui.UnbluNavUtil

@SuppressLint("ServiceCast")
@Composable
fun SuccessScreen(
    navController: NavController,
    viewModel: UnbluScreenViewModel,
    onNavigateBack: () -> Unit
) {
    // State for showing the logout confirmation dialog
    val showDialog by viewModel.showDialog
    val unbluView by viewModel.mainView
    val chatUiOpen by viewModel.chatUiOpen

    Scaffold(

    ) { it ->
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .focusable(true),
                factory = {
                    unbluView?.detachView()
                    unbluView?.apply { configView(this) }
                    unbluView ?: RelativeLayout(it)
                })

            AnimatedVisibility(visible = !chatUiOpen,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.setShowDialog(true) }) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                CustomAlertDialog(
                    onCancel = { viewModel.setShowDialog(false) },
                    title = stringResource(id = R.string.logout_confirmation_title),
                    message = stringResource(id = R.string.logout_confirmation_message),
                    cancelText = stringResource(id = R.string.logout_confirmation_negative),
                    confirmText = stringResource(id = R.string.logout_confirmation_positive),
                    onConfirm = {
                        goBack(onNavigateBack, unbluView, navController)
                    },
                    alertDialogColors = AlertDialogColorDefaults.alertDialogColors()
                )
            }
        }

        BackHandler() {
            unbluView?.let {
                UnbluNavUtil.getUnbluNav(unbluView)?.goBack { didGoBack ->
                    if (!didGoBack) {
                        viewModel.setShowDialog(true)
                    }
                }
            } ?: run {
                goBack(onNavigateBack, unbluView, navController)
            }
        }

        LaunchedEffect(viewModel) {
            viewModel.sessionEnded.collect {
                goBack(onNavigateBack, unbluView, navController)
            }
        }
    }
}

private fun goBack(
    onNavigateBack: () -> Unit,
    unbluView: View?,
    navController: NavController
) {
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

@Preview
@Composable
fun PreviewSuccessScreen() {
    // Mock ViewModel and NavController
    val mockNavController = rememberNavController()
    val mockViewModel = remember { UnbluScreenViewModel() }
    val mockOnNavigateBack = {}

    SuccessScreen(
        navController = mockNavController,
        viewModel = mockViewModel,
        onNavigateBack = mockOnNavigateBack
    )
}