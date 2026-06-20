package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.components.fmtPhone
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

    val subC    = if (isDark) AppColors.SubDark    else AppColors.SubLight
    val borderC = if (isDark) AppColors.BorderDark  else AppColors.BorderLight
    val cardBg  = if (isDark) AppColors.CardDark    else AppColors.CardLight
    val arrowC  = if (isDark) AppColors.HintDark    else AppColors.HintLight

    val contacts = appData.contacts
    fun phoneFor(role: String): String? {
        val v = contacts.find { it.role == role }?.phone?.trim()
        return if (v.isNullOrEmpty()) null else v
    }
    fun nameFor(role: String): String =
        contacts.find { it.role == role }?.name?.trim() ?: ""
    fun dial(phone: String) {
        val digits = phone.replace(Regex("\\D"), "")
        if (digits.isNotEmpty()) ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
    }

    val skoSupervisorPhone  = phoneFor("SKO 관리감독자")
    val sktSafetyPhone      = phoneFor("SKT 안전관리자")
    val skoResponsiblePhone = phoneFor("SKO 총괄책임자")
    val sktSupervisorPhone  = phoneFor("SKT 관리감독자")

    val skoSupervisorName  = nameFor("SKO 관리감독자")
    val sktSafetyName      = nameFor("SKT 안전관리자")
    val skoResponsibleName = nameFor("SKO 총괄책임자")
    val sktSupervisorName  = nameFor("SKT 관리감독자")

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
        BoxWithConstraints(Modifier.fillMaxSize().padding(inner)) {
            val availableH = maxHeight

            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = availableH)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
                    .padding(top = 8.dp, bottom = 10.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = (availableH - 18.dp).coerceAtLeast(240.dp))
                        .border(1.dp, borderC, RoundedCornerShape(14.dp))
                        .background(cardBg, RoundedCornerShape(14.dp))
                        .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                ) {
                    Text("보고 체계", fontSize = 13.sp, fontWeight = FontWeight.W700, color = subC, letterSpacing = 0.3.sp)

                    Spacer(Modifier.weight(1f))
                    FlowNode(
                        label = "최초 목격자",
                        nodeBg = if (isDark) Color(0xFF2A0A0A) else Color(0xFFFCEBEB),
                        nodeText = if (isDark) Color(0xFFFF8080) else Color(0xFFA32D2D),
                        modifier = Modifier.fillMaxWidth(0.78f).align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.weight(0.15f))
                    ArrowDown(arrowC)
                    Spacer(Modifier.weight(0.15f))
                    FlowNode(
                        label = "SKO 관리감독자",
                        nodeBg = if (isDark) Color(0xFF2A1800) else Color(0xFFFAEEDA),
                        nodeText = if (isDark) Color(0xFFF5A623) else Color(0xFF854F0B),
                        modifier = Modifier.fillMaxWidth(0.78f).align(Alignment.CenterHorizontally),
                        name = skoSupervisorName,
                        phone = skoSupervisorPhone,
                        onClick = {
                            if (skoSupervisorPhone != null) dial(skoSupervisorPhone)
                            else android.widget.Toast.makeText(ctx, "연락처 등록 필요", android.widget.Toast.LENGTH_SHORT).show()
                        },
                    )
                    Spacer(Modifier.weight(0.15f))
                    BranchArrow(arrowC)
                    Spacer(Modifier.weight(0.15f))
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FlowNodeHalf(
                            label = "SKT 안전관리자",
                            nodeBg = if (isDark) Color(0xFF0D1E3A) else Color(0xFFE6F1FB),
                            nodeText = if (isDark) Color(0xFF5BA3F5) else Color(0xFF185FA5),
                            modifier = Modifier.weight(1f),
                            name = sktSafetyName,
                            phone = sktSafetyPhone,
                            onClick = {
                                if (sktSafetyPhone != null) dial(sktSafetyPhone)
                                else android.widget.Toast.makeText(ctx, "연락처 등록 필요", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        )
                        FlowNodeHalf(
                            label = "SKO 총괄책임자",
                            nodeBg = if (isDark) Color(0xFF0D1E3A) else Color(0xFFE6F1FB),
                            nodeText = if (isDark) Color(0xFF5BA3F5) else Color(0xFF185FA5),
                            modifier = Modifier.weight(1f),
                            name = skoResponsibleName,
                            phone = skoResponsiblePhone,
                            onClick = {
                                if (skoResponsiblePhone != null) dial(skoResponsiblePhone)
                                else android.widget.Toast.makeText(ctx, "연락처 등록 필요", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                    Spacer(Modifier.weight(0.15f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { ArrowDown(arrowC) }
                        Box(Modifier.weight(1f)) { ArrowDown(arrowC) }
                    }
                    Spacer(Modifier.weight(0.15f))
                    Row(
                        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FlowNodeHalf(
                            label = "SKT 관리감독자",
                            nodeBg = if (isDark) Color(0xFF0A1A0A) else Color(0xFFEAF3DE),
                            nodeText = if (isDark) Color(0xFF52B847) else Color(0xFF27500A),
                            modifier = Modifier.weight(1f),
                            name = sktSupervisorName,
                            phone = sktSupervisorPhone,
                            onClick = {
                                if (sktSupervisorPhone != null) dial(sktSupervisorPhone)
                                else android.widget.Toast.makeText(ctx, "연락처 등록 필요", android.widget.Toast.LENGTH_SHORT).show()
                            },
                        )
                        FlowNodeHalf(
                            label = "SKO CSPO",
                            nodeBg = if (isDark) Color(0xFF0A1A0A) else Color(0xFFEAF3DE),
                            nodeText = if (isDark) Color(0xFF52B847) else Color(0xFF27500A),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FlowNode(
    label: String,
    nodeBg: Color,
    nodeText: Color,
    modifier: Modifier = Modifier.fillMaxWidth(),
    name: String? = null,
    phone: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val hasName  = !name.isNullOrEmpty()
    val hasPhone = !phone.isNullOrEmpty()
    Box(
        modifier
            .heightIn(min = 60.dp)
            .background(nodeBg, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.W700, color = nodeText, textAlign = TextAlign.Center)
            if (hasName) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasPhone) {
                        Icon(Icons.Outlined.PhoneInTalk, null, Modifier.size(with(LocalDensity.current) { 13.sp.toDp() }), tint = nodeText)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(name!!, fontSize = 13.sp, fontWeight = FontWeight.W500, color = nodeText.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun FlowNodeHalf(
    label: String,
    nodeBg: Color,
    nodeText: Color,
    modifier: Modifier,
    name: String? = null,
    phone: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val hasName  = !name.isNullOrEmpty()
    val hasPhone = !phone.isNullOrEmpty()
    Box(
        modifier
            .fillMaxHeight()
            .heightIn(min = 60.dp)
            .background(nodeBg, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.W700, color = nodeText, textAlign = TextAlign.Center)
            if (hasName) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasPhone) {
                        Icon(Icons.Outlined.PhoneInTalk, null, Modifier.size(with(LocalDensity.current) { 12.sp.toDp() }), tint = nodeText)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(name!!, fontSize = 13.sp, fontWeight = FontWeight.W500, color = nodeText.copy(alpha = 0.9f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ArrowDown(arrowC: Color) {
    Box(Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.ArrowDownward, contentDescription = null, tint = arrowC, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun BranchArrow(arrowC: Color, modifier: Modifier = Modifier.fillMaxWidth()) {
    val gapDp = 8.dp
    Canvas(modifier.height(42.dp)) {
        val w = size.width
        val h = size.height
        val gapPx = gapDp.toPx()
        val cardW = (w - gapPx) / 2f
        val lx = cardW / 2f
        val rx = cardW + gapPx + cardW / 2f
        val cx = w / 2f
        val branchY = h * 0.42f
        val sw = 2.0.dp.toPx()
        val arrowLen = 7.dp.toPx()

        drawLine(arrowC, Offset(cx, 0f),   Offset(cx, branchY), sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(lx, branchY), Offset(rx, branchY), sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(lx, branchY), Offset(lx, h),   sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(rx, branchY), Offset(rx, h),   sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(lx, h), Offset(lx - arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(lx, h), Offset(lx + arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(rx, h), Offset(rx - arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
        drawLine(arrowC, Offset(rx, h), Offset(rx + arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
    }
}

