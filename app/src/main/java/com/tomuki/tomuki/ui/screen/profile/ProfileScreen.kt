package com.tomuki.tomuki.ui.screen.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.twotone.Help
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomuki.tomuki.ui.theme.TomukiTheme

@Composable
fun MenuItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Set appropriate content description
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}


@Composable
@Preview
fun ProfileScreen(){
    TomukiTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(1f) // Set the desired width percentage
                    .padding(16.dp)
                    .height(72.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Login,
                        contentDescription = "Login Icon"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Вход не выполнен",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Войти в аккаунт TomuID",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Divider(thickness = 1.dp, 
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 8.dp))
            Column {
                MenuItem(icon = Icons.Filled.History, text = "История")
                MenuItem(icon = Icons.Filled.Savings, text = "Поддержать проект")
                MenuItem(icon = Icons.Filled.Settings, text = "Настройки")
            }
            Divider(thickness = 1.dp,
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 8.dp))
            Column {
                MenuItem(icon = Icons.Filled.SystemUpdate, text = "Проверить обновления")
                MenuItem(icon = Icons.TwoTone.Help, text = "Помощь")

            }
        }
    }
}