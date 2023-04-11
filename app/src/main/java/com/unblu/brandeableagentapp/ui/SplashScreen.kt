package com.unblu.brandeableagentapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.unblu.brandeableagentapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.splash_screen_background)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.logo),
            //TODO remove this color filter
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
    }
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("login")
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        Surface {
            SplashScreen(navController)
        }
    }
}
