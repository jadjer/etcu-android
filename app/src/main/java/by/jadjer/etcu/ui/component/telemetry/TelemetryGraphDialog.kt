package by.jadjer.etcu.ui.component.telemetry

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun TelemetryGraphDialog(
    title: String,
    value: String,
    unit: String,
    history: List<Int>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        TelemetryGraphDialogContent(
            title = title,
            value = value,
            unit = unit,
            history = history,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun TelemetryGraphDialogContent(
    title: String,
    value: String,
    unit: String,
    history: List<Int>,
    onDismiss: () -> Unit
) {
    val minVal = history.minOrNull()
    val maxVal = history.maxOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Min
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_min),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = minVal?.toString() ?: "-",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Current
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.unit_format, unit),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Max
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_max),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = maxVal?.toString() ?: "-",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TelemetryGraph(
                data = history,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TelemetryGraphDialogPreview() {
    ETCUTheme {
        TelemetryGraphDialogContent(
            title = "Engine RPM",
            value = "2500",
            unit = "RPM",
            history = listOf(1000, 1500, 2000, 2500, 2200, 2400, 2500, 2300, 2100, 2000),
            onDismiss = {}
        )
    }
}
