package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun StatusIndicator(
    label: String,
    isActive: Boolean,
    activeText: String = stringResource(R.string.common_active),
    inactiveText: String = stringResource(R.string.common_inactive),
    icon: ImageVector? = null
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, null, Modifier.size(24.dp), tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                }
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
            Surface(color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                Text(if (isActive) activeText else inactiveText, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusIndicatorPreview() {
    ETCUTheme {
        StatusIndicator(label = "Engine Status", isActive = true)
    }
}
