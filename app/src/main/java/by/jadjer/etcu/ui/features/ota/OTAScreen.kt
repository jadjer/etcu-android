package by.jadjer.etcu.ui.features.ota

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import java.util.regex.Pattern

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

                if (!state.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.ota_release_notes),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val annotatedString = buildAnnotatedString {
                                val pattern =
                                    Pattern.compile("(https?://[\\w\\d:#@%/;$()~_?+\\-=.&]*)")
                                val matcher = pattern.matcher(state.description)
                                var lastIndex = 0
                                while (matcher.find()) {
                                    append(state.description.substring(lastIndex, matcher.start()))
                                    val url = matcher.group()
                                    pushLink(LinkAnnotation.Url(url))
                                    withStyle(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    ) {
                                        append(url)
                                    }
                                    pop()
                                    lastIndex = matcher.end()
                                }
                                append(state.description.substring(lastIndex))
                            }
                            Text(
                                text = annotatedString,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

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
                Text(
                    stringResource(R.string.ota_up_to_date),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_retry))
                }
            }

            is OTAState.Downloading -> {
                Text(
                    stringResource(
                        R.string.ota_downloading_progress,
                        (state.progress * 100).toInt()
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is OTAState.Uploading -> {
                Text(
                    stringResource(
                        R.string.ota_uploading_progress,
                        (state.progress * 100).toInt()
                    )
                )
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
                        stringResource(
                            R.string.ota_chunks_remaining,
                            state.totalChunks - state.currentChunk
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val seconds = (state.estimatedTimeMs / 1000) % 60
                    val minutes = (state.estimatedTimeMs / (1000 * 60)) % 60
                    val timeStr = "%02d:%02d".format(minutes, seconds)

                    Text(
                        stringResource(R.string.ota_time_remaining, timeStr),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OTAState.Success -> {
                Text(
                    stringResource(R.string.ota_success),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCheckUpdates) {
                    Text(stringResource(R.string.btn_done))
                }
            }

            is OTAState.Error -> {
                Text(
                    stringResource(R.string.ota_error_generic, state.message),
                    color = MaterialTheme.colorScheme.error
                )
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
            state = OTAState.Uploading(
                progress = 0.5f,
                currentChunk = 50,
                totalChunks = 100,
                firmwareSize = 51200,
                estimatedTimeMs = 5000
            ),
            onCheckUpdates = {},
            onStartUpdate = { _, _ -> }
        )
    }
}
