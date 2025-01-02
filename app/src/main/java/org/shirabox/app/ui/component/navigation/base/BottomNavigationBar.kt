package org.shirabox.app.ui.component.navigation.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController, items: List<BottomNavItems> = navItems) {
    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = remember(navBackStackEntry.value) {
            navBackStackEntry.value?.destination?.route
        }

        items.forEach { item ->
            val selected = remember(currentRoute) {
                (currentRoute == item.route) || (item.children.contains(currentRoute))
            }

            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = Color(0xFF9E9E9E)

            val color by animateColorAsState(
                targetValue = if (selected) selectedColor else unselectedColor,
                animationSpec = tween(100),
                label = ""
            )

            val icon = if (selected) item.selectedIcon else item.icon

            val interactionSource = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .selectable(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        },
                        enabled = true,
                        role = Role.Tab,
                        interactionSource = interactionSource,
                        indication = null,
                    )
                    .fillMaxWidth()
                    .weight(1.0F, false)
                    .defaultMinSize(minHeight = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(icon),
                    contentDescription = "${item.name} Icon",
                    tint = color
                )
                Text(
                    text = stringResource(item.name),
                    fontSize = 13.sp,
                    color = color,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}