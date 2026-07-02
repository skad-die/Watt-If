package com.skaddie.watt_if.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.skaddie.watt_if.ui.theme.WattIfDimens
import com.skaddie.watt_if.ui.theme.WattIfTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("PH").build())
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(
                message = "Saved ${currencyFormat.format(uiState.estimatedBill)} to history",
                duration = SnackbarDuration.Short
            )
            viewModel.onSavedConsumed()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        HomeScreenContent(
            uiState = uiState,
            onCurrentKwhChange = viewModel::onCurrentKwhChange,
            onPreviousKwhChange = viewModel::onPreviousKwhChange,
            onRateChange = viewModel::onRateChange,
            onSave = { viewModel.saveReading() },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onCurrentKwhChange: (String) -> Unit,
    onPreviousKwhChange: (String) -> Unit,
    onRateChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = WattIfDimens.ScreenPaddingHorizontal,
                vertical = WattIfDimens.ScreenPaddingVertical
            ),
        verticalArrangement = Arrangement.spacedBy(WattIfDimens.SectionSpacing)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(WattIfDimens.TitleSpacing)) {
            Text(
                text = "Watt If",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Estimate your electricity bill",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(WattIfDimens.FieldSpacing)) {
            ReadingField(
                label = "Current kWh reading",
                value = uiState.currentKwh,
                onValueChange = onCurrentKwhChange,
                error = uiState.currentKwhError
            )
            ReadingField(
                label = "Previous kWh reading",
                value = uiState.previousKwh,
                onValueChange = onPreviousKwhChange,
                optional = true
            )
            ReadingField(
                label = "Rate per kWh (₱)",
                value = uiState.rate,
                onValueChange = onRateChange,
                error = uiState.rateError
            )
        }

        BillSummaryCard(
            consumption = uiState.consumption,
            estimatedBill = uiState.estimatedBill
        )

        Button(
            onClick = onSave,
            shape = RoundedCornerShape(WattIfDimens.ButtonCornerRadius),
            modifier = Modifier
                .fillMaxWidth()
                .height(WattIfDimens.ButtonHeight)
        ) {
            Text(
                text = "Save reading",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ReadingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    optional: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            shape = RoundedCornerShape(WattIfDimens.FieldCornerRadius),
            color = if (error != null)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(
                        text = if (optional) "$label (optional)" else label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (error != null)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = if (error != null)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WattIfDimens.FieldHeight)
            )
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun BillSummaryCard(consumption: Double, estimatedBill: Double) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(
            Locale.Builder().setLanguage("en").setRegion("PH").build()
        )
    }

    Card(
        shape = RoundedCornerShape(WattIfDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WattIfDimens.CardPaddingHorizontal,
                    vertical = WattIfDimens.CardPaddingVertical
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(WattIfDimens.CardInnerSpacing)) {
                Text(
                    "Consumption",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${"%.1f".format(consumption)} kWh",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(WattIfDimens.CardInnerSpacing)
            ) {
                Text(
                    "Estimated bill",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    currencyFormat.format(estimatedBill),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewLight() {
    WattIfTheme(darkTheme = false, dynamicColor = false) {
        HomeScreenContent(
            uiState = HomeUiState("1248.5", "1190.0", "12.50", 58.5, 731.25),
            onCurrentKwhChange = {}, onPreviousKwhChange = {}, onRateChange = {}, onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewDark() {
    WattIfTheme(darkTheme = true, dynamicColor = false) {
        HomeScreenContent(
            uiState = HomeUiState("1248.5", "1190.0", "12.50", 58.5, 731.25),
            onCurrentKwhChange = {}, onPreviousKwhChange = {}, onRateChange = {}, onSave = {}
        )
    }
}