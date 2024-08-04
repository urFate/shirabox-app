package org.shirabox.app.ui.component.navigation.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun ExploreNavigationBar(navController: NavController, items: List<ExploreNavItems> = ExploreNavItems.navItems) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = remember(navBackStackEntry.value) {
        navBackStackEntry.value?.destination?.route
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            val selected = remember(currentRoute) { currentRoute == item.route }

            ExploreChip(selected = selected, label = { Text(text = stringResource(id = item.name)) }) {
                navController.navigate(item.route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreChip(
    selected: Boolean,
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors().copy(
        labelColor = MaterialTheme.colorScheme.primary,
        leadingIconColor = MaterialTheme.colorScheme.primary
    )
    val borderBrush = Brush.linearGradient(
        if (!selected) listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary
        ) else listOf(Color.Transparent, Color.Transparent))

    val border = FilterChipDefaults.filterChipBorder(true, selected).copy(
        brush = borderBrush
    )

    FilterChip(
        onClick = onClick,
        selected = selected,
        label = label,
        colors = chipColors,
        border = border
    )
}