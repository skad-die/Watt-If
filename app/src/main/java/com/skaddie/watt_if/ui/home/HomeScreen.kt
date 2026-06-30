package com.skaddie.watt_if.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.skaddie.watt_if.ui.theme.WattIfDimens
import com.skaddie.watt_if.ui.theme.WattIfTextSizes
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCalculate: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WattIfDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(WattIfDimens.SectionSpacing)
    ) {
        Text(
            text = "Watt If",
            fontSize = WattIfTextSizes.TitleLarge,
            fontWeight = FontWeight.Bold
        )

        RoundedField(
            label = "Current kWh reading",
            value = uiState.currentKwh,
            onValueChange = viewModel::onCurrentKwhChange
        )
        RoundedField(
            label = "Previous kWh reading",
            value = uiState.previousKwh,
            onValueChange = viewModel::onPreviousKwhChange
        )
        RoundedField(
            label = "Rate per kWh (₱)",
            value = uiState.rate,
            onValueChange = viewModel::onRateChange
        )

        BillPreviewCard(
            consumption = uiState.consumption,
            estimatedBill = uiState.estimatedBill
        )

        Button(
            onClick = {
                viewModel.saveReading()
                onCalculate()
            },
            shape = RoundedCornerShape(WattIfDimens.CornerRadiusButton),
            modifier = Modifier
                .fillMaxWidth()
                .height(WattIfDimens.ButtonHeight)
        ) {
            Text(
                "Calculate bill",
                fontSize = WattIfTextSizes.ButtonText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BillPreviewCard(consumption: Double, estimatedBill: Double) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("PH").build())
    }
    Card(
        shape = RoundedCornerShape(WattIfDimens.CornerRadiusField),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WattIfDimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Consumption",
                    fontSize = WattIfTextSizes.StatLabel,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "${"%.1f".format(consumption)} kWh",
                    fontSize = WattIfTextSizes.StatValue,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Estimated bill",
                    fontSize = WattIfTextSizes.StatLabel,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    currencyFormat.format(estimatedBill),
                    fontSize = WattIfTextSizes.StatValue,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun RoundedField(label: String, value: String, onValueChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(WattIfDimens.CornerRadiusField),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = WattIfTextSizes.FieldLabel) },
            textStyle = TextStyle(
                fontSize = WattIfTextSizes.FieldInput,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(WattIfDimens.FieldHeight)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}