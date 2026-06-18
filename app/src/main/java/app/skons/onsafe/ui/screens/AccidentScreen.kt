package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import kotlinx.coroutines.delay

private data class StepData(
    val num: Int, val title: String, val desc: String, val emoji: String,
    val dotLight: Color, val dotDark: Color,
)

private val steps = listOf(
    StepData(1, "즉시 작업 중지",   "전원 차단 · 출입 통제",               "🛑", AppColors.Red,    AppColors.RedDark),
    StepData(2, "구호 · 응급처치",  "의식·호흡 확인, CPR 필요 시 즉시 실시", "🚑", Color(0xFFCC3300), Color(0xFFFF5533)),
    StepData(3, "119 신고",         "안전신고센터에 즉시 신고",              "📞", AppColors.Red,    AppColors.RedDark),
    StepData(4, "근로자 대피",       "안전구역 이동, 2차 사고 방지",          "🏃", AppColors.Orange, AppColors.OrangeDark),
    StepData(5, "관리책임자 보고",   "SKT · SKO 책임자 동시 전파",           "📞", AppColors.Blue,   AppColors.BlueDark),
)

@Composable
fun AccidentScreen(
    navController: NavHostController,
    locationViewModel: LocationViewModel,
    isDark: Boolean,
    onMenuClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val locState by locationViewModel.state.collectAsStateWithLifecycle()

    var blinkOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        locationViewModel.fetch()
        while (true) { delay(500L); blinkOn = !blinkOn }
    }

    val blinkColor = if (blinkOn) (if (isDark) AppColors.RedDark else AppColors.Red) else AppColors.BlinkOrange
    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
    val lineC = if (isDark) AppColors.BorderDark else AppColors.BorderLight

    fun sendSms119() {
        val addr = if (locState.locationEnabled && locState.status == LocationStatus.Ready) locState.address else null
        val body = Uri.encode(
            if (addr != null) "119 신고합니다.\n현재 위치: $addr\n사고가 발생하였습니다. 즉시 출동 요청합니다."
            else "119 신고합니다.\n사고가 발생하였습니다. 즉시 출동 요청합니다."
        )
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:119?body=$body")))
    }

    Scaffold(
        topBar = {
            DetailAppBar(
                title = "사고 발생",
                isDark = isDark,
                currentRoute = "accident",
                onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() },
                onMenuClick = onMenuClick,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        },
        containerColor = if (isDark) AppColors.BgDark else AppColors.BgLight,
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            // 119 blink card
            Box(
                Modifier
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
                        HomeActionBtnA("전화", Icons.Default.Phone) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
                        }
                        Spacer(Modifier.width(8.dp))
                        HomeActionBtnA("문자", Icons.Default.Sms) { sendSms119() }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Timeline — 5개 스텝이 남은 공간을 균등 분배
            Column(Modifier.weight(1f)) {
                steps.forEachIndexed { i, step ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left: connecting line + numbered circle
                        Box(
                            Modifier
                                .width(36.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            // 위쪽 연결선 (첫 번째 제외)
                            if (i > 0) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .width(2.dp)
                                        .fillMaxHeight(0.5f)
                                        .background(lineC),
                                )
                            }
                            // 아래쪽 연결선 (마지막 제외)
                            if (i < steps.size - 1) {
                                Box(
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .width(2.dp)
                                        .fillMaxHeight(0.5f)
                                        .background(lineC),
                                )
                            }
                            // Numbered circle
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(if (isDark) step.dotDark else step.dotLight, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "${step.num}", fontSize = 15.sp, fontWeight = FontWeight.W800,
                                    color = Color.White,
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        // Right: content
                        Row(
                            Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(step.title, fontSize = 16.sp, fontWeight = FontWeight.W700, color = textC)
                                Spacer(Modifier.height(3.dp))
                                Text(step.desc, fontSize = 13.sp, color = subC)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(step.emoji, fontSize = 26.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionBtnA(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onTap: () -> Unit) {
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
