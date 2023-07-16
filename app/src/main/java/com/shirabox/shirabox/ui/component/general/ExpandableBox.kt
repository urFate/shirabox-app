package com.shirabox.shirabox.ui.component.general

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableBox(
    modifier: Modifier = Modifier,
    startHeight: Dp,
    fadeEffect: Boolean = true,
    disposable: Boolean = false,
    onExpand: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
){
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val colorScheme: ColorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isExpanded = if(disposable) true else !isExpanded
                onExpand.invoke()
            }
            .fillMaxWidth()
            .animateContentSize()
            .then(modifier)
            .also {
                if(disposable && isExpanded) Modifier.clickable(false){}
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = if(isExpanded) Modifier
                    .fillMaxHeight()
                else Modifier
                    .height(startHeight)
                    .then(
                        if(fadeEffect)
                            Modifier.drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(
                                                    colorScheme.background.red,
                                                    colorScheme.background.green,
                                                    colorScheme.background.blue,
                                                    0.3f
                                                ),
                                                colorScheme.background
                                            ),
                                            startY = 90.0f,
                                        ), blendMode = BlendMode.SrcAtop
                                    )
                                }
                            }
                        else Modifier
                    ),
                content = content
            )

            if(!disposable) {
                Icon(
                    imageVector = if(isExpanded)
                        Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = "Expand More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if(disposable && !isExpanded) {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = "Expand More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}