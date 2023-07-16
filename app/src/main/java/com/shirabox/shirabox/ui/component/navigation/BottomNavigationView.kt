package com.shirabox.shirabox.ui.component.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@ExperimentalFoundationApi
@Composable
fun BottomNavigationView() {

    val navController = rememberNavController()

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) {
        Box(modifier = Modifier.padding(it)) {
            ShiraBoxNavHost(navController)
        }
    }
}