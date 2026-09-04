package by.jadjer.etcu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.system.SystemError
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun ErrorsBlock(activeErrors: List<SystemError>) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(stringResource(R.string.diag_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        if (activeErrors.isEmpty()) {
            Box(Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text(stringResource(R.string.diag_no_errors), color = Color(0xFF2E7D32))
            }
        } else {
            activeErrors.forEach { error ->
                Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Text(stringResource(R.string.diag_error_item, stringResource(error.resId)), color = Color(0xFFC62828), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorsBlockPreview() {
    ETCUTheme {
        ErrorsBlock(activeErrors = listOf(SystemError.ACCELERATOR_CALIBRATE_FAULT))
    }
}
