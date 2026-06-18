package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.ContactCard
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import kotlinx.coroutines.delay

@Composable
fun ContactsScreen(
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
    LaunchedEffect(Unit) {
        locationViewModel.fetch()
        while (true) { delay(500L); blinkOn = !blinkOn }
    }

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
            DetailAppBar(
                title = "긴급 연락처",
                isDark = isDark,
                currentRoute = "contacts",
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
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
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
                        ContactActionBtn("전화", Icons.Default.Phone) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
                        }
                        Spacer(Modifier.width(8.dp))
                        ContactActionBtn("문자", Icons.Default.Sms) { sendSms119() }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Contact list
            appData.contacts.forEach { c ->
                ContactCard(
                    contact = c,
                    isDark = isDark,
                    onTap = {
                        val phone = c.phone.replace(Regex("\\D"), "")
                        if (phone.isNotEmpty()) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }
                    },
                )
                Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun ContactActionBtn(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onTap: () -> Unit) {
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
