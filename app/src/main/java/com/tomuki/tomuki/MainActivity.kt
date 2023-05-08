package com.tomuki.tomuki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tomuki.tomuki.ui.theme.TomukiTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomukiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Menu()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "main")
@Composable
fun Menu(){
    val bottomNavItems = listOf(
        BottomNavItem(
            name = stringResource(R.string.explore),
            route = "explore",
            icon = Icons.Outlined.Explore,
        ),
        BottomNavItem(
            name = stringResource(R.string.favourites),
            route = "favourites",
            icon = Icons.Outlined.BookmarkBorder,
        ),
        BottomNavItem(
            name = stringResource(R.string.profile),
            route = "profile",
            icon = Icons.Outlined.AccountCircle,
        ),
    )

    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()

    TomukiTheme {
        Scaffold(
            bottomBar = {
                androidx.compose.material3.NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = item.route == backStackEntry.value?.destination?.route

                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigate(item.route) },
                            label = {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = "${item.name} Icon",
                                )
                            }
                        )
                    }
                }
            },
            content = {
                Row(
                    modifier = Modifier.padding(it),
                    content = {
                        Text( text = "hello world")
                    }
                )
            }
        )
    }

}