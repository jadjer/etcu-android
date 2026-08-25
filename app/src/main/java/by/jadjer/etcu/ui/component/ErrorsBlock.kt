package by.jadjer.etcu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.jadjer.etcu.domain.model.SystemError

@Composable
fun ErrorsBlock(activeErrors: List<SystemError>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Статус диагностики:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (activeErrors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(text = "❌ Ошибок не обнаружено", color = Color(0xFF2E7D32))
            }
        } else {
            activeErrors.forEach { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ ${error.description}",
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ErrorsBlockPreview() {
    ErrorsBlock(
        listOf(
            SystemError.ACCELERATOR_CALIBRATE_FAULT,
            SystemError.SERVO_READ_ERROR,
        )
    )
}
