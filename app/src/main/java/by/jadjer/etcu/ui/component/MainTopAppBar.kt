package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.theme.ETCUTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(connectionStatus: String) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = { Text(connectionStatus, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 16.dp)) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

@Preview(showBackground = true)
@Composable
private fun MainTopAppBarPreview() {
    ETCUTheme {
        MainTopAppBar(connectionStatus = "Connected")
    }
}
