package by.jadjer.etcu.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.jadjer.etcu.R
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun PlayStoreBanner() {
    Box(
        modifier = Modifier
            .size(width = 1024.dp, height = 500.dp)
            .background(Color(0xFF006494)), // Наш основной синий цвет
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип (переиспользуем ic_throttle)
            Image(
                painter = painterResource(id = R.drawable.ic_throttle),
                contentDescription = null,
                modifier = Modifier.size(180.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Название приложения
            Text(
                text = stringResource(id = R.string.app_name),
                color = Color.White,
                fontSize = 86.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            // Описание
            Text(
                text = "Electronic Throttle Control Unit",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(widthDp = 1024, heightDp = 500)
@Composable
private fun PlayStoreBannerPreview() {
    ETCUTheme {
        PlayStoreBanner()
    }
}
