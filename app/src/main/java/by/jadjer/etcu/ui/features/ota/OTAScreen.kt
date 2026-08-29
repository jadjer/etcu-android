package by.jadjer.etcu.ui.features.ota

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R

@Composable
fun OtaScreen(viewModel: OtaViewModel) {
    val state by viewModel.state.collectAsState()
    
    OtaScreenContent(
        state = state,
        onCheckUpdates = { viewModel.checkForUpdates() },
        onStartUpdate = { url, size -> viewModel.startUpdate(url, size) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtaScreenContent(
    state: OTAState,
    onCheckUpdates: () -> Unit,
    onStartUpdate: (String, Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.ota_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        when (state) {
            OTAState.Idle -> {
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_check_updates))
                }
            }
            OTAState.CheckingUpdates -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.ota_searching))
            }
            is OTAState.UpdateAvailable -> {
                Text(
                    text = stringResource(R.string.ota_current_version, state.currentVersion),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.ota_update_available, state.latestVersion),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onStartUpdate(state.downloadUrl, state.size) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_download_install))
                }
            }
            OTAState.UpToDate -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.ota_up_to_date), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_retry))
                }
            }
            is OTAState.Downloading -> {
                Text(stringResource(R.string.ota_downloading_progress, (state.progress * 100).toInt()))
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is OTAState.Uploading -> {
                Text(stringResource(R.string.ota_uploading_progress, (state.progress * 100).toInt()))
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(R.string.ota_firmware_size, state.firmwareSize / 1024f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.ota_chunks_total, state.totalChunks),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.ota_chunks_transferred, state.currentChunk),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.ota_chunks_remaining, state.totalChunks - state.currentChunk),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            OTAState.Success -> {
                Text(stringResource(R.string.ota_success), color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_done))
                }
            }
            is OTAState.Error -> {
                Text(stringResource(R.string.ota_error_generic, state.message), color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_try_again))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtaScreenPreview() {
    MaterialTheme {
        OtaScreenContent(
            state = OTAState.Uploading(0.45f, 45, 100, 20480),
            onCheckUpdates = {},
            onStartUpdate = { _, _ -> }
        )
    }
}
