package app.skons.onsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.components.Emergency119Card
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel

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
    val locState by locationViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { locationViewModel.fetch() }

    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
    val lineC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    // 폰트 스케일 제곱으로 shouldScroll 임계값을 높여 큰 폰트에서 클리핑 방지
    val fontScale = LocalDensity.current.fontScale
    val address = if (locState.locationEnabled && locState.status == LocationStatus.Ready) locState.address else null

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
            Emergency119Card(isDark = isDark, address = address)
            Spacer(Modifier.height(10.dp))

            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val estStepH = (61f * fontScale * fontScale).dp
                val shouldScroll = estStepH * steps.size > maxHeight

                Column(
                    Modifier.fillMaxSize()
                        .then(if (shouldScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                ) {
                    steps.forEachIndexed { i, step ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .then(if (shouldScroll) Modifier.padding(vertical = 10.dp) else Modifier.weight(1f)),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.width(36.dp).fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (i > 0) {
                                    Box(
                                        Modifier
                                            .align(Alignment.TopCenter)
                                            .width(2.dp)
                                            .fillMaxHeight(0.5f)
                                            .background(lineC),
                                    )
                                }
                                if (i < steps.size - 1) {
                                    Box(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .width(2.dp)
                                            .fillMaxHeight(0.5f)
                                            .background(lineC),
                                    )
                                }
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
}
