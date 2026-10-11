package com.example.echomuse.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.echomuse.model.EqualizerPreset
import com.example.echomuse.ui.MusicUiState

@Composable
fun EqualizerDialog(
    uiState: MusicUiState,
    onDismiss: () -> Unit,
    onPresetSelect: (EqualizerPreset) -> Unit
) {
    val currentEq = uiState.currentEqualizerPreset
    var band60 by remember(currentEq) { mutableFloatStateOf(currentEq.band60Hz) }
    var band230 by remember(currentEq) { mutableFloatStateOf(currentEq.band230Hz) }
    var band910 by remember(currentEq) { mutableFloatStateOf(currentEq.band910Hz) }
    var band3600 by remember(currentEq) { mutableFloatStateOf(currentEq.band3600Hz) }
    var band14000 by remember(currentEq) { mutableFloatStateOf(currentEq.band14000Hz) }
    var isBassBoostEnabled by remember { androidx.compose.runtime.mutableStateOf(currentEq.bassBoost > 0.3f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Graphic Equalizer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Presets Horizontal Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.equalizerPresets) { preset ->
                        val isSelected = preset.id == currentEq.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPresetSelect(preset) },
                            label = { Text(preset.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Frequency Bands",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bands Sliders
                EqBandSlider("60 Hz", band60) { band60 = it }
                EqBandSlider("230 Hz", band230) { band230 = it }
                EqBandSlider("910 Hz", band910) { band910 = it }
                EqBandSlider("3.6 kHz", band3600) { band3600 = it }
                EqBandSlider("14 kHz", band14000) { band14000 = it }

                Spacer(modifier = Modifier.height(12.dp))

                // Bass Boost Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bass Boost Effect",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isBassBoostEnabled,
                        onCheckedMethod = { isBassBoostEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Apply & Close")
            }
        }
    )
}

@Composable
private fun EqBandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(52.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -10f..10f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary
            )
        )
        Text(
            text = "${if (value >= 0) "+" else ""}${value.toInt()}dB",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(44.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Switch(
    checked: Boolean,
    onCheckedMethod: (Boolean) -> Unit,
    colors: androidx.compose.material3.SwitchColors
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedMethod,
        colors = colors
    )
}
