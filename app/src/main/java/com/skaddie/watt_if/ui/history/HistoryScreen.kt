package com.skaddie.watt_if.ui.history

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.skaddie.watt_if.data.local.entity.ReadingEntity
import com.skaddie.watt_if.ui.theme.WattIfDimens
import com.skaddie.watt_if.ui.theme.WattIfTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val readings by viewModel.readings.collectAsState()

    HistoryScreenContent(
        readings = readings,
        onDelete = { reading ->
            viewModel.deleteReading(reading)
        },
        onUndoDelete = { reading ->
            viewModel.undoDelete(reading)
        }
    )
}

@Composable
fun HistoryScreenContent(
    readings: List<ReadingEntity>,
    onDelete: (ReadingEntity) -> Unit,
    onUndoDelete: (ReadingEntity) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<ReadingEntity?>(null) }

    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(
            Locale.Builder().setLanguage("en").setRegion("PH").build()
        )
    }
    val dateFormat = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        actionColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                },
                modifier = Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = WattIfDimens.ScreenPaddingHorizontal,
                    vertical = WattIfDimens.ScreenPaddingVertical
                ),
            verticalArrangement = Arrangement.spacedBy(WattIfDimens.SectionSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(WattIfDimens.TitleSpacing)) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your past electricity readings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (readings.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(WattIfDimens.FieldSpacing)
                ) {
                    items(
                        items = readings,
                        key = { it.id }
                    ) { reading ->
                        val requestDelete: () -> Unit = { pendingDelete = reading }

                        SwipeToDeleteReadingCard(
                            reading = reading,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            onDelete = requestDelete,
                            onLongClick = requestDelete
                        )
                    }
                }
            }
        }

        pendingDelete?.let { readingToDelete ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete reading?", style = MaterialTheme.typography.titleMedium) },
                text = { Text("Are you sure you want to delete this reading?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDelete(readingToDelete)
                            pendingDelete = null
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Reading deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoDelete(readingToDelete)
                                }
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) {
                        Text("Cancel")
                    }
                },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteReadingCard(
    reading: ReadingEntity,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(WattIfDimens.CardCornerRadius))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = WattIfDimens.CardPaddingHorizontal),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete reading",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        ReadingCard(
            reading = reading,
            currencyFormat = currencyFormat,
            dateFormat = dateFormat,
            onLongClick = onLongClick
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadingCard(
    reading: ReadingEntity,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onLongClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(WattIfDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {}, // Keeps the layer structured, but consider handling a proper detailed-view click here later!
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WattIfDimens.CardPaddingHorizontal,
                    vertical = WattIfDimens.CardPaddingVertical
                ),
            verticalArrangement = Arrangement.spacedBy(WattIfDimens.CardSectionSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    // 3. Formats direct long values safely avoiding redundant Date object overhead
                    text = dateFormat.format(reading.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${currencyFormat.format(reading.ratePerKwh)}/kWh",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = WattIfDimens.DividerThickness
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(WattIfDimens.CardInnerSpacing)) {
                    Text(
                        text = "Consumption",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format(reading.consumption)} kWh",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(WattIfDimens.CardInnerSpacing)
                ) {
                    Text(
                        text = "Total bill",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(reading.totalBill),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WattIfDimens.TitleSpacing)
        ) {
            Text(
                text = "No readings yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Save a reading from the Home screen to see it here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreviewEmpty() {
    WattIfTheme(darkTheme = false, dynamicColor = false) {
        HistoryScreenContent(
            readings = emptyList(),
            onDelete = {},
            onUndoDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreviewWithData() {
    WattIfTheme(darkTheme = false, dynamicColor = false) {
        HistoryScreenContent(
            readings = listOf(
                ReadingEntity(
                    id = 1,
                    currentKwh = 1248.5,
                    previousKwh = 1190.0,
                    ratePerKwh = 12.50,
                    consumption = 58.5,
                    totalBill = 731.25,
                    date = System.currentTimeMillis()
                ),
                ReadingEntity(
                    id = 2,
                    currentKwh = 1190.0,
                    previousKwh = 1130.0,
                    ratePerKwh = 12.50,
                    consumption = 60.0,
                    totalBill = 750.00,
                    date = System.currentTimeMillis() - 2592000000L
                )
            ),
            onDelete = {},
            onUndoDelete = {}
        )
    }
}