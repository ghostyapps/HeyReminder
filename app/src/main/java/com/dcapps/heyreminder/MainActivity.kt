package com.dcapps.heyreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dcapps.heyreminder.ui.theme.ReminderAppTheme
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private val viewModel: BatteryStatusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BatteryStatusScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun BatteryStatusScreen(viewModel: BatteryStatusViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val timeSinceFullCharge = uiState.lastFullChargeTimestamp?.let { lastFull ->
        if (lastFull <= currentTimeMillis) currentTimeMillis - lastFull else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BatteryPercentageCard(percentage = uiState.batteryPercentage)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                title = "Last full charge",
                value = uiState.lastFullChargeTimestamp?.let { formatTimestamp(it) } ?: "Unknown"
            )

            InfoCard(
                modifier = Modifier.weight(1f),
                title = "Time since full",
                value = timeSinceFullCharge?.let { formatDuration(it) } ?: "Unknown"
            )
        }

        InfoCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Estimated remaining",
            value = uiState.estimatedRemainingTimeMillis?.let { formatDuration(it) } ?: "Unavailable"
        )
    }
}

@Composable
private fun BatteryPercentageCard(percentage: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Battery",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Start
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return formatter.format(Date(timestamp))
}

private fun formatDuration(durationMillis: Long): String {
    if (durationMillis <= 0) return "Just now"

    val totalMinutes = durationMillis / 60000
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60

    val parts = mutableListOf<String>()
    if (days > 0) parts += "${days}d"
    if (hours > 0) parts += "${hours}h"
    if (minutes > 0) parts += "${minutes}m"

    return parts.joinToString(" ").ifEmpty { "<1m" }
}
