package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.navigateMain
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.LocalDarkTheme
import app.skons.onsafe.viewmodel.ContactViewModel

@Composable
fun ReportFlowScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    onMenuClick: () -> Unit,
) {
    val isDark = LocalDarkTheme.current
    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()

    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val cardBg  = if (isDark) AppColors.CardDark   else AppColors.CardLight
    val subC    = if (isDark) AppColors.SubDark     else AppColors.SubLight
    val arrowC  = if (isDark) AppColors.HintDark    else AppColors.HintLight

    val contacts = appData.contacts
    fun phoneFor(role: String): String? {
        val v = contacts.find { it.role == role }?.phone?.trim()
        return if (v.isNullOrEmpty()) null else v
    }
    fun nameFor(role: String): String = contacts.find { it.role == role }?.name?.trim() ?: ""
    fun dial(phone: String) {
        val digits = phone.replace(Regex("\\D"), "")
        if (digits.isNotEmpty()) ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
    }
    fun noContact() = android.widget.Toast.makeText(ctx, "연락처 등록 필요", android.widget.Toast.LENGTH_SHORT).show()

    val skoSupervisorPhone  = phoneFor("SKO 관리감독자")
    val sktSafetyPhone      = phoneFor("SKT 안전관리자")
    val skoResponsiblePhone = phoneFor("SKO 총괄책임자")
    val sktSupervisorPhone  = phoneFor("SKT 관리감독자")

    val redBg        = if (isDark) Color(0xFF2A0A0A) else Color(0xFFFCEBEB)
    val redAccent    = if (isDark) Color(0xFFFF8080) else Color(0xFFA32D2D)
    val orangeBg     = if (isDark) Color(0xFF2A1800) else Color(0xFFFAEEDA)
    val orangeAccent = if (isDark) Color(0xFFF5A623) else Color(0xFF854F0B)
    val blueBg       = if (isDark) Color(0xFF0D1E3A) else Color(0xFFE6F1FB)
    val blueAccent   = if (isDark) Color(0xFF5BA3F5) else Color(0xFF185FA5)
    val greenBg      = if (isDark) Color(0xFF0A1A0A) else Color(0xFFEAF3DE)
    val greenAccent  = if (isDark) Color(0xFF52B847) else Color(0xFF27500A)

    Scaffold(
        topBar = {
            DetailAppBar(
                title = "보고 체계",
                currentRoute = "report",
                onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() },
                onMenuClick = onMenuClick,
                onNavigate = { navController.navigateMain(it) },
            )
        },
        containerColor = if (isDark) AppColors.BgDark else AppColors.BgLight,
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderC, RoundedCornerShape(14.dp))
                    .background(cardBg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            ) {
                Text("보고 체계", fontSize = 12.sp, fontWeight = FontWeight.W700, color = subC, letterSpacing = 0.3.sp)
                Spacer(Modifier.height(12.dp))

                // 최초 목격자 - 가운데 정렬, 배지 없음, 레이블 없음
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FlowCard(
                        category = "",
                        title = "최초 목격자",
                        description = "",
                        nodeBg = redBg, accent = redAccent,
                        phone = null, showBadge = false, centered = true,
                        modifier = Modifier.fillMaxWidth(0.8f),
                    )
                }
                FlowArrow(arrowC)

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FlowCard(
                        category = "SKO",
                        title = "관리감독자",
                        description = nameFor("SKO 관리감독자"),
                        nodeBg = orangeBg, accent = orangeAccent,
                        phone = skoSupervisorPhone,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onClick = { if (skoSupervisorPhone != null) dial(skoSupervisorPhone) else noContact() },
                    )
                }
                BranchArrow(arrowC)

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowCard(
                        category = "SKT", title = "안전관리자",
                        description = nameFor("SKT 안전관리자"),
                        nodeBg = blueBg, accent = blueAccent,
                        phone = sktSafetyPhone, compact = true,
                        modifier = Modifier.weight(1f),
                        onClick = { if (sktSafetyPhone != null) dial(sktSafetyPhone) else noContact() },
                    )
                    FlowCard(
                        category = "SKO", title = "총괄책임자",
                        description = nameFor("SKO 총괄책임자"),
                        nodeBg = blueBg, accent = blueAccent,
                        phone = skoResponsiblePhone, compact = true,
                        modifier = Modifier.weight(1f),
                        onClick = { if (skoResponsiblePhone != null) dial(skoResponsiblePhone) else noContact() },
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { FlowArrow(arrowC) }
                    Box(Modifier.weight(1f)) { FlowArrow(arrowC) }
                }

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowCard(
                        category = "SKT", title = "관리감독자",
                        description = nameFor("SKT 관리감독자"),
                        nodeBg = greenBg, accent = greenAccent,
                        phone = sktSupervisorPhone, compact = true,
                        modifier = Modifier.weight(1f),
                        onClick = { if (sktSupervisorPhone != null) dial(sktSupervisorPhone) else noContact() },
                    )
                    // SKO CSPO - 배지 없음
                    FlowCard(
                        category = "SKO", title = "CSPO",
                        description = "",
                        nodeBg = greenBg, accent = greenAccent,
                        phone = null, showBadge = false, compact = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun FlowCard(
    category: String,
    title: String,
    description: String,
    nodeBg: Color,
    accent: Color,
    phone: String?,
    noPhoneLabel: String? = null,
    showBadge: Boolean = true,
    centered: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val hasPhone    = !phone.isNullOrEmpty()
    val hasDesc     = description.isNotEmpty()
    val hasCategory = category.isNotEmpty()
    val circleSize   = 30.dp
    val iconSize     = 15.dp
    val circleSizeLg = 34.dp
    val iconSizeLg   = 17.dp

    Box(
        (if (compact) modifier.fillMaxHeight() else modifier)
            .background(nodeBg, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick.invoke() } else Modifier)
            .padding(horizontal = 14.dp, vertical = if (centered) 21.dp else 12.dp),
        contentAlignment = if (centered) Alignment.Center else Alignment.TopStart,
    ) {
        when {
            compact -> {
                Column(Modifier.fillMaxHeight()) {
                    if (hasCategory) Text(category, fontSize = 11.sp, color = accent.copy(alpha = 0.75f), fontWeight = FontWeight.W800)
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.W800, color = accent)
                    Spacer(Modifier.height(3.dp))
                    Text(description, fontSize = 13.sp, color = accent.copy(alpha = 0.8f), fontWeight = FontWeight.W500)
                    Spacer(Modifier.weight(1f))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (hasPhone) {
                            Box(
                                Modifier.size(circleSize).background(accent, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.PhoneInTalk, null, tint = nodeBg, modifier = Modifier.size(iconSize))
                            }
                        } else if (showBadge) {
                            Box(
                                Modifier.background(accent.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                            ) {
                                Text(noPhoneLabel ?: "번호 없음", fontSize = 11.sp, color = accent.copy(alpha = 0.7f), fontWeight = FontWeight.W600, maxLines = 1)
                            }
                        }
                    }
                }
            }
            centered -> {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (hasCategory) {
                        Text(category, fontSize = 11.sp, color = accent.copy(alpha = 0.75f), fontWeight = FontWeight.W600, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.W800, color = accent, textAlign = TextAlign.Center)
                    if (hasPhone) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier
                                .background(accent, RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.PhoneInTalk, null, tint = nodeBg, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("전화", fontSize = 13.sp, fontWeight = FontWeight.W700, color = nodeBg)
                        }
                    } else if (showBadge) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier.background(accent.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(noPhoneLabel ?: "번호 없음", fontSize = 12.sp, color = accent.copy(alpha = 0.7f), fontWeight = FontWeight.W600)
                        }
                    }
                }
            }
            else -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val label = if (hasCategory) "$category $title" else title
                        Text(label, fontSize = 16.sp, fontWeight = FontWeight.W800, color = accent, textAlign = TextAlign.Center)
                        if (hasDesc) {
                            Spacer(Modifier.height(3.dp))
                            Text(description, fontSize = 13.sp, color = accent.copy(alpha = 0.8f), fontWeight = FontWeight.W500, textAlign = TextAlign.Center)
                        }
                        if (!hasPhone && showBadge) {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier.background(accent.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(noPhoneLabel ?: "번호 없음", fontSize = 11.sp, color = accent.copy(alpha = 0.7f), fontWeight = FontWeight.W600, maxLines = 1)
                            }
                        }
                    }
                    if (hasPhone) {
                        Box(
                            Modifier.align(Alignment.CenterEnd).size(circleSizeLg)
                                .background(accent, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.PhoneInTalk, null, tint = nodeBg, modifier = Modifier.size(iconSizeLg))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowArrow(arrowC: Color) {
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.ArrowDownward, null, tint = arrowC, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun BranchArrow(arrowC: Color) {
    val gapDp = 8.dp
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Canvas(Modifier.fillMaxWidth().height(42.dp)) {
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

            drawLine(arrowC, Offset(cx, 0f), Offset(cx, branchY), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(lx, branchY), Offset(rx, branchY), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(lx, branchY), Offset(lx, h), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(rx, branchY), Offset(rx, h), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(lx, h), Offset(lx - arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(lx, h), Offset(lx + arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(rx, h), Offset(rx - arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
            drawLine(arrowC, Offset(rx, h), Offset(rx + arrowLen, h - arrowLen), sw, cap = StrokeCap.Round)
        }
    }
}
