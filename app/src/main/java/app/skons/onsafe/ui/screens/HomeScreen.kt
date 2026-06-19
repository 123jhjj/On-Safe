package app.skons.onsafe.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
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
import app.skons.onsafe.ui.components.Emergency119Card
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

    var backPressed by remember { mutableStateOf(false) }
    BackHandler {
        if (backPressed) {
            (ctx as? Activity)?.finish()
        } else {
            backPressed = true
            android.widget.Toast.makeText(ctx, "뒤로 버튼을 한번 더 누르면 종료됩니다", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) { locationViewModel.fetch() }
    LaunchedEffect(backPressed) { if (backPressed) { delay(2000L); backPressed = false } }

    val address = if (locState.locationEnabled && locState.status == LocationStatus.Ready) locState.address else null

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
            Emergency119Card(isDark = isDark, address = address)
            Spacer(Modifier.height(7.dp))

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
