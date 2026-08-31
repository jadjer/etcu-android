package by.jadjer.etcu.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.domain.model.SystemError
import by.jadjer.etcu.ui.theme.ETCUTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorsBottomSheet(activeErrors: List<SystemError>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Box(Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            ErrorsBlock(activeErrors)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorsBottomSheetPreview() {
    ETCUTheme {
        // ModalBottomSheet content preview
        Box(Modifier.padding(16.dp)) {
            ErrorsBlock(listOf(SystemError.SERVO_COMMS_ERROR))
        }
    }
}
