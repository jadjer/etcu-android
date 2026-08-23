package by.jadjer.etcu.ui.screen.ota

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OtaScreen(viewModel: OtaViewModel) {
    val state by viewModel.state.collectAsState()
    
    OtaScreenContent(
        state = state,
        onCheckUpdates = { viewModel.checkForUpdates() },
        onStartUpdate = { url -> viewModel.startUpdate(url) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtaScreenContent(
    state: OtaState,
    onCheckUpdates: () -> Unit,
    onStartUpdate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Обновление прошивки", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        when (state) {
            OtaState.Idle -> {
                Button(onClick = onCheckUpdates) {
                    Text("Проверить обновления")
                }
            }
            OtaState.CheckingUpdates -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Поиск новой версии...")
            }
            is OtaState.UpdateAvailable -> {
                Text("Доступна новая версия: ${state.version}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onStartUpdate(state.downloadUrl) }) {
                    Text("Скачать и установить")
                }
            }
            OtaState.Downloading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Скачивание прошивки...")
            }
            is OtaState.Uploading -> {
                Text("Передача прошивки: ${(state.progress * 100).toInt()}%")
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Чанк ${state.currentChunk} из ${state.totalChunks}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            OtaState.Success -> {
                Text("Обновление успешно завершено!", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckUpdates) {
                    Text("Готово")
                }
            }
            is OtaState.Error -> {
                Text("Ошибка: ${state.message}", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckUpdates) {
                    Text("Попробовать снова")
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
            onStartUpdate = {}
        )
    }
}
