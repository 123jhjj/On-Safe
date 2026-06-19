package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current

    val subC    = if (isDark) AppColors.SubDark    else AppColors.SubLight
    val borderC = if (isDark) AppColors.BorderDark  else AppColors.BorderLight
    val cardBg  = if (isDark) AppColors.CardDark    else AppColors.CardLight
    val arrowC  = if (isDark) AppColors.HintDark    else AppColors.HintLight

    var contactsSectionHeightPx by remember { mutableIntStateOf(0) }

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
        val contacts = appData.contacts

        BoxWithConstraints(
            Modifier.fillMaxSize().padding(inner),
        ) {
            val availableH = maxHeight
            // top8 + bottom10 = 18dp 외부패딩 + 8dp 섹션간격 = 26dp
            val gapAndPaddingPx = with(density) { 26.dp.roundToPx() }

            // 연락처 섹션 실측값 기반으로 흐름도 높이 결정
            // 최솟값 220dp: 극소형 폰에서도 노드가 읽힐 수 있는 하한
            val flowChartH = with(density) {
                (availableH.roundToPx() - gapAndPaddingPx - contactsSectionHeightPx)
                    .coerceAtLeast(220.dp.roundToPx())
                    .toDp()
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = availableH)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
                    .padding(top = 8.dp, bottom = 10.dp),
            ) {
                // 흐름도 카드 — 실측 기반 높이, SpaceEvenly로 노드 균등 배분
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = flowChartH)
                        .border(1.dp, borderC, RoundedCornerShape(14.dp))
                        .background(cardBg, RoundedCornerShape(14.dp))
                        .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text("보고 체계", fontSize = 13.sp, fontWeight = FontWeight.W700, color = subC, letterSpacing = 0.3.sp)
                    FlowNode(
                        "최초 목격자",
                        if (isDark) Color(0xFF2A0A0A) else Color(0xFFFCEBEB),
                        if (isDark) Color(0xFFFF8080) else Color(0xFFA32D2D),
                    )
                    ArrowDown(arrowC)
                    FlowNode(
                        "SKO 관리감독자",
                        if (isDark) Color(0xFF2A1800) else Color(0xFFFAEEDA),
                        if (isDark) Color(0xFFF5A623) else Color(0xFF854F0B),
                    )
                    ArrowDown(arrowC)
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
                        Box(Modifier.weight(1f)) { ArrowDown(arrowC) }
                        Box(Modifier.weight(1f)) { ArrowDown(arrowC) }
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

                // 연락처 섹션 — onSizeChanged로 실제 렌더링 높이 측정
                Column(
                    Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size -> contactsSectionHeightPx = size.height }
                        .border(1.dp, borderC, RoundedCornerShape(14.dp))
                        .background(cardBg, RoundedCornerShape(14.dp))
                        .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                ) {
                    Text("동시 전파 연락처", fontSize = 13.sp, fontWeight = FontWeight.W700, color = subC, letterSpacing = 0.3.sp)
                    Spacer(Modifier.height(8.dp))
                    contacts.forEachIndexed { i, c ->
                        if (i > 0) Spacer(Modifier.height(5.dp))
                        ContactCard(
                            contact = c,
                            isDark = isDark,
                            modifier = Modifier.fillMaxWidth(),
                            onTap = {
                                val phone = c.phone.replace(Regex("\\D"), "")
                                if (phone.isNotEmpty()) {
                                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                } else {
                                    android.widget.Toast.makeText(ctx, "연락처 등록 필요", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowNode(label: String, nodeBg: Color, nodeText: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(nodeBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.W700, color = nodeText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FlowNodeHalf(label: String, nodeBg: Color, nodeText: Color, modifier: Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .background(nodeBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.W700, color = nodeText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ArrowDown(arrowC: Color) {
    Box(Modifier.fillMaxWidth().padding(vertical = 1.dp), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.ArrowDownward, contentDescription = null, tint = arrowC, modifier = Modifier.size(20.dp))
    }
}
