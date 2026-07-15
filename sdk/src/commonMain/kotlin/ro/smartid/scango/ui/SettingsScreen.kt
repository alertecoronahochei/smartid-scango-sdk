package ro.smartid.scango.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScanSettings

private val ITEM_LIMITS = listOf(0, 10, 25, 50)

/**
 * The SDK's settings page: which symbologies to look for, plus the behaviour toggles
 * a scan&go flow actually needs. Every change is persisted immediately.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SettingsScreen(
    settings: ScanSettings,
    strings: ScanGoStrings,
    onSettingsChanged: (ScanSettings) -> Unit,
    onResetDefaults: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    TextButton(onClick = onResetDefaults) { Text(strings.resetDefaults) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { SectionHeader(strings.sectionFormats) }
            item {
                Text(
                    text = strings.sectionFormatsHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    BarcodeFormat.entries.forEach { format ->
                        val selected = format in settings.enabledFormats
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val updated = if (selected) {
                                    settings.enabledFormats - format
                                } else {
                                    settings.enabledFormats + format
                                }
                                // Never let the user disable everything — a scanner that scans
                                // nothing is a bug report waiting to happen.
                                if (updated.isNotEmpty()) {
                                    onSettingsChanged(settings.copy(enabledFormats = updated))
                                }
                            },
                            label = { Text(format.displayName) },
                        )
                    }
                }
            }
            if (settings.enabledFormats.isEmpty()) {
                item {
                    Text(
                        text = strings.noFormatSelected,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 12.dp)) }
            item { SectionHeader(strings.sectionBehavior) }
            item {
                SwitchRow(
                    title = strings.continuousScan,
                    subtitle = strings.continuousScanHint,
                    checked = settings.continuousScan,
                    onCheckedChange = { onSettingsChanged(settings.copy(continuousScan = it)) },
                )
            }
            item {
                SwitchRow(
                    title = strings.allowDuplicates,
                    subtitle = strings.allowDuplicatesHint,
                    checked = settings.allowDuplicates,
                    onCheckedChange = { onSettingsChanged(settings.copy(allowDuplicates = it)) },
                )
            }
            item {
                SwitchRow(
                    title = strings.torchOnStart,
                    subtitle = strings.torchOnStartHint,
                    checked = settings.torchOnStart,
                    onCheckedChange = { onSettingsChanged(settings.copy(torchOnStart = it)) },
                )
            }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(strings.maxItems, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = strings.maxItemsHint(settings.maxItems),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ITEM_LIMITS.forEach { limit ->
                            FilterChip(
                                selected = settings.maxItems == limit,
                                onClick = { onSettingsChanged(settings.copy(maxItems = limit)) },
                                label = { Text(if (limit == 0) "∞" else limit.toString()) },
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 12.dp)) }
            item { SectionHeader(strings.sectionFeedback) }
            item {
                SwitchRow(
                    title = strings.beepOnScan,
                    subtitle = null,
                    checked = settings.beepOnScan,
                    onCheckedChange = { onSettingsChanged(settings.copy(beepOnScan = it)) },
                )
            }
            item {
                SwitchRow(
                    title = strings.vibrateOnScan,
                    subtitle = null,
                    checked = settings.vibrateOnScan,
                    onCheckedChange = { onSettingsChanged(settings.copy(vibrateOnScan = it)) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
