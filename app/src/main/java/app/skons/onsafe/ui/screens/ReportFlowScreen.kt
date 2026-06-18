package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.ContactCard
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.ContactViewModel

@Composable
fun ReportFlowScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    isDark: Boolean,
    onMenuClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()

    val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val cardBg = if (isDark) AppColors.CardDark else AppColors.CardLight
    val arrowC = if (isDark) AppColors.HintDark else AppColors.HintLight

    @Composable
    fun FlowNode(label: String, nodeBg: Color, nodeText: Color) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(nodeBg, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.W700, color = nodeText, textAlign = TextAlign.Center)
        }
    }

    @Composable
    fun FlowNodeHalf(label: String, nodeBg: Color, nodeText: Color, modifier: Modifier) {
        Box(
            modifier
                .background(nodeBg, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.W700, color = nodeText, textAlign = TextAlign.Center)
        }
    }

    @Composable
    fun ArrowDown() {
        Box(Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ArrowDownward, contentDescription = null, tint = arrowC, modifier = Modifier.size(22.dp))
        }
    }

    Scaffold(
        topBar = {
            DetailAppBar(
                title = "보고 체계",
                isDark = isDark,
                currentRoute = "report",
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
            // Flow chart card
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderC, RoundedCornerShape(14.dp))
                    .background(cardBg, RoundedCornerShape(14.dp))
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text("보고 체계", fontSize = 13.sp, fontWeight = FontWeight.W700, color = subC, letterSpacing = 0.3.sp)
                Spacer(Modifier.height(8.dp))
                FlowNode(
                    "최초 목격자",
                    if (isDark) Color(0xFF2A0A0A) else Color(0xFFFCEBEB),
                    if (isDark) Color(0xFFFF8080) else Color(0xFFA32D2D),
                )
                ArrowDown()
                FlowNode(
                    "SKO 관리감독자",
                    if (isDark) Color(0xFF2A1800) else Color(0xFFFAEEDA),
                    if (isDark) Color(0xFFF5A623) else Color(0xFF854F0B),
                )
                ArrowDown()
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowNodeHalf("SKT 안전관리자",
                        if (isDark) Color(0xFF0D1E3A) else Color(0xFFE6F1FB),
                        if (isDark) Color(0xFF5BA3F5) else Color(0xFF185FA5),
                        Modifier.weight(1f),
                    )
                    FlowNodeHalf("SKO 총괄책임자",
                        if (isDark) Color(0xFF0D1E3A) else Color(0xFFE6F1FB),
                        if (isDark) Color(0xFF5BA3F5) else Color(0xFF185FA5),
                        Modifier.weight(1f),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { ArrowDown() }
                    Box(Modifier.weight(1f)) { ArrowDown() }
                }
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowNodeHalf("SKT 관리감독자",
                        if (isDark) Color(0xFF0A1A0A) else Color(0xFFEAF3DE),
                        if (isDark) Color(0xFF52B847) else Color(0xFF27500A),
                        Modifier.weight(1f),
                    )
                    FlowNodeHalf("SKO CSPO",
                        if (isDark) Color(0xFF0A1A0A) else Color(0xFFEAF3DE),
                        if (isDark) Color(0xFF52B847) else Color(0xFF27500A),
                        Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Contact list card
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderC, RoundedCornerShape(14.dp))
                    .background(cardBg, RoundedCornerShape(14.dp))
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
            ) {
                Text("동시 전파 연락처", fontSize = 13.sp, fontWeight = FontWeight.W700, color = subC, letterSpacing = 0.3.sp)
                Spacer(Modifier.height(8.dp))
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
                    Spacer(Modifier.height(5.dp))
                }
            }
        }
    }
}
