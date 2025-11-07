package com.rejown.qrcraft.presentation.generator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.CodeCustomization
import com.rejown.qrcraft.domain.models.ErrorCorrectionLevel

@Composable
fun CustomizationPanel(
    isExpanded: Boolean,
    customization: CodeCustomization,
    onToggleExpanded: () -> Unit,
    onCustomizationChanged: (CodeCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Size slider
                    Column {
                        Text(
                            text = "Size: ${customization.size}px",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = customization.size.toFloat(),
                            onValueChange = { newSize ->
                                onCustomizationChanged(
                                    customization.copy(size = newSize.toInt())
                                )
                            },
                            valueRange = 256f..2048f,
                            steps = 7 // 256, 512, 768, 1024, 1280, 1536, 1792, 2048
                        )
                    }

                    // Color pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Foreground color
                        ColorPicker(
                            label = "Foreground",
                            color = Color(customization.foregroundColor),
                            onColorSelected = { color ->
                                onCustomizationChanged(
                                    customization.copy(foregroundColor = color.toArgb())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Background color
                        ColorPicker(
                            label = "Background",
                            color = Color(customization.backgroundColor),
                            onColorSelected = { color ->
                                onCustomizationChanged(
                                    customization.copy(backgroundColor = color.toArgb())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Error correction level (simplified buttons)
                    Column {
                        Text(
                            text = "Error Correction",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ErrorCorrectionLevel.entries.forEach { level ->
                                ErrorCorrectionChip(
                                    level = level,
                                    isSelected = customization.errorCorrectionLevel == level,
                                    onClick = {
                                        onCustomizationChanged(
                                            customization.copy(errorCorrectionLevel = level)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPicker(
    label: String,
    color: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Preset colors
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val colors = listOf(
                Color.Black,
                Color.White,
                Color.Red,
                Color.Blue,
                Color.Green
            )

            colors.forEach { presetColor ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(presetColor)
                        .border(
                            width = if (color == presetColor) 2.dp else 1.dp,
                            color = if (color == presetColor) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(presetColor) }
                )
            }
        }
    }
}

@Composable
private fun ErrorCorrectionChip(
    level: ErrorCorrectionLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.name.first().toString(),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
