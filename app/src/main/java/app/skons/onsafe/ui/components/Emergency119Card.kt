package app.skons.onsafe.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun Emergency119Card(
    isDark: Boolean,
    address: String?,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    var blinkOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay(500L); blinkOn = !blinkOn } }
    val blinkColor = if (blinkOn) (if (isDark) AppColors.RedDark else AppColors.Red) else AppColors.BlinkOrange

    fun sendSms() {
        val body = Uri.encode(
            if (address != null) "119 신고합니다.\n현재 위치: $address\n사고가 발생하였습니다. 즉시 출동 요청합니다."
            else "119 신고합니다.\n사고가 발생하였습니다. 즉시 출동 요청합니다."
        )
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:119?body=$body")))
    }

    Box(
        modifier
            .fillMaxWidth()
            .background(blinkColor, RoundedCornerShape(16.dp))
            .clickable { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"))) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("119 신고", fontSize = 24.sp, fontWeight = FontWeight.W800, color = Color.White)
                Text("안전신고센터", fontSize = 14.sp, color = Color.White.copy(alpha = 0.78f))
            }
            Row {
                Emergency119Btn("전화", Icons.Default.Phone) {
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
                }
                Spacer(Modifier.width(8.dp))
                Emergency119Btn("문자", Icons.Default.Sms) { sendSms() }
            }
        }
    }
}

@Composable
private fun Emergency119Btn(label: String, icon: ImageVector, onTap: () -> Unit) {
    Box(
        Modifier
            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.W700, color = Color.White)
        }
    }
}
