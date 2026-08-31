package by.jadjer.etcu.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun ErrorFab(errorCount: Int, onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) {
        BadgedBox(badge = { Badge { Text(errorCount.toString()) } }) {
            Icon(Icons.Default.Warning, stringResource(R.string.common_errors))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorFabPreview() {
    ETCUTheme {
        ErrorFab(errorCount = 3, onClick = {})
    }
}
