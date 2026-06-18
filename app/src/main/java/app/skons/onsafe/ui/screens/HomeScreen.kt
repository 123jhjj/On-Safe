package app.skons.onsafe.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.HomeAppBar
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import kotlinx.coroutines.delay

private data class BtnData(
    val label: String, val sub: String, val icon: ImageVector, val route: String,
    val colorLight: Color, val colorDark: Color,
)

private val homeButtons = listOf(
    BtnData("사고 발생",   "5단계 행동요령 즉시 표시",    Icons.Outlined.Warning,     "accident", AppColors.Red,    AppColors.RedDark),
    BtnData("보고 체계",   "보고 체계 + 동시 전파 연락",  Icons.Outlined.AccountTree,  "report",   AppColors.Orange, AppColors.OrangeDark),
    BtnData("긴급 연락처", "탭하면 바로 전화 연결",        Icons.Outlined.PhoneInTalk,  "contacts", AppColors.Blue,   AppColors.BlueDark),
    BtnData("보고 양식",   "빈칸 채워 즉시 전송",          Icons.Outlined.Assignment,   "script",   AppColors.Green,  AppColors.GreenDark),
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    isDark: Boolean,
    onMenuClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()
    val locState by locationViewModel.state.collectAsStateWithLifecycle()

    var blinkOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay(500L); blinkOn = !blinkOn } }
    LaunchedEffect(Unit) { locationViewModel.fetch() }

    var backPressed by remember { mutableStateOf(false) }
    BackHandler {
        if (backPressed) {
            (ctx as? Activity)?.finish()
        } else {
            backPressed = true
            android.widget.Toast.makeText(ctx, "뒤로 버튼을 한번 더 누르면 종료됩니다", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(backPressed) { if (backPressed) { delay(2000L); backPressed = false } }

    val blinkColor = if (blinkOn) (if (isDark) AppColors.RedDark else AppColors.Red) else AppColors.BlinkOrange

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
            HomeAppBar(
                company = appData.myInfo.company,
                isDark = isDark,
                currentRoute = "home",
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
            // 119 blinking card
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
                        HomeActionBtn("전화", Icons.Default.Phone) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
                        }
                        Spacer(Modifier.width(8.dp))
                        HomeActionBtn("문자", Icons.Default.Sms) { sendSms119() }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 2×2 grid — 남은 공간을 차지하며 세로 중앙 정렬
            Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HomeBigCard(homeButtons[0], isDark, Modifier.weight(1f).fillMaxHeight()) { navController.navigate(homeButtons[0].route) }
                        HomeBigCard(homeButtons[1], isDark, Modifier.weight(1f).fillMaxHeight()) { navController.navigate(homeButtons[1].route) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HomeBigCard(homeButtons[2], isDark, Modifier.weight(1f).fillMaxHeight()) { navController.navigate(homeButtons[2].route) }
                        HomeBigCard(homeButtons[3], isDark, Modifier.weight(1f).fillMaxHeight()) { navController.navigate(homeButtons[3].route) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionBtn(label: String, icon: ImageVector, onTap: () -> Unit) {
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

@Composable
private fun HomeBigCard(btn: BtnData, isDark: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = if (isDark) btn.colorDark else btn.colorLight
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color,
        shadowElevation = 3.dp,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(btn.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(btn.label, fontSize = 20.sp, fontWeight = FontWeight.W800, color = Color.White, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(5.dp))
            Text(btn.sub, fontSize = 12.5.sp, color = Color.White.copy(alpha = 0.82f), maxLines = 2)
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Icon(
                    Icons.Default.ArrowForwardIos, contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}
