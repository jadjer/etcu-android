package by.jadjer.etcu.ui.screen.errors

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.data.model.SystemError
import by.jadjer.etcu.ui.component.ErrorsBlock

@Composable
fun ErrorsScreen(viewModel: ErrorsViewModel) {
    val errors by viewModel.activeErrors.collectAsState()
    ErrorsScreenContent(errors)
}

@Composable
fun ErrorsScreenContent(errors: List<SystemError>) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ErrorsBlock(activeErrors = errors)
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorsScreenPreview() {
    MaterialTheme {
        ErrorsScreenContent(
            errors = listOf(
                SystemError.GUARD_LOCK,
                SystemError.SERVO_COMMS_ERROR
            )
        )
    }
}
