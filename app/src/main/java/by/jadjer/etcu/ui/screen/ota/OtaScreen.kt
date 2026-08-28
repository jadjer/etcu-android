package by.jadjer.etcu.ui.screen.ota

import androidx.compose.foundation.layout.*
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
    state: OtaState,
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
            OtaState.Idle -> {
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_check_updates))
                }
            }
            OtaState.CheckingUpdates -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.ota_searching))
            }
            is OtaState.UpdateAvailable -> {
                Text(stringResource(R.string.ota_update_available, state.version))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onStartUpdate(state.downloadUrl, state.size) }) {
                    Text(stringResource(R.string.btn_download_install))
                }
            }
            is OtaState.Downloading -> {
                Text(stringResource(R.string.ota_downloading_progress, (state.progress * 100).toInt()))
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is OtaState.Uploading -> {
                Text(stringResource(R.string.ota_uploading_progress, (state.progress * 100).toInt()))
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.ota_chunk_info, state.currentChunk, state.totalChunks),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            OtaState.Success -> {
                Text(stringResource(R.string.ota_success), color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_done))
                }
            }
            is OtaState.Error -> {
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
            state = OtaState.Uploading(0.45f, 45, 100),
            onCheckUpdates = {},
            onStartUpdate = { _, _ -> }
        )
    }
}
