package com.unblu.brandeableagentapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.unblu.brandeableagentapp.model.LoginViewModel
import com.unblu.brandeableagentapp.model.UnbluScreenViewModel
import com.unblu.brandeableagentapp.nav.NavGraph
import com.unblu.brandeableagentapp.ui.theme.BrandeableAgentAppTheme
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.application.UnbluApplicationHelper
import com.unblu.sdk.core.callback.BackButtonCallback
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainActivity : ComponentActivity() {
    val compositeDisposable =  CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelStore = ViewModelStore()
        val unbluController = (application as AgentApplication).unbluController
        ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[LoginViewModel::class.java].setUnbluController(unbluController)
        val unbluScreenViewModel = ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[UnbluScreenViewModel::class.java]

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
            }

            BrandeableAgentAppTheme {
                NavGraph(ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application)))
            }
        }

        compositeDisposable.addAll(
            Unblu
                .onAgentInitialized()
                .subscribe(
                    { agentClient ->
                        agentClient.setBackButtonCallback { hasBackStack-> 
                            if(!hasBackStack) {unbluScreenViewModel.setShowDialog(true)
                                return@setBackButtonCallback false
                            }
                            else {
                                true
                            }
                        }
                        unbluScreenViewModel.setMainView(agentClient.mainView)
                    },
                    { error -> Log.e("MainActivity", "Error: ${error.localizedMessage}") }
                ),
            Unblu
                .onUiHideRequest()
                .subscribe (
                    {
                        unbluScreenViewModel.setShowDialog(true)
                    },
                    { error -> Log.e("MainActivity", "Error: ${error.localizedMessage}") })
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.apply {
            UnbluApplicationHelper.onNewIntent(this.extras)
        }
    }
    override fun onDestroy() {
        viewModelStore.clear()
        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BrandeableAgentAppTheme {
        Greeting("Android")
    }
}