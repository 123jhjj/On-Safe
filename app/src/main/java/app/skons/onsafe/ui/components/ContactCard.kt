package app.skons.onsafe.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.ui.theme.AppColors

internal fun fmtPhone(raw: String): String {
    if (raw.contains('-')) return raw
    val d = raw.replace(Regex("\\D"), "")
    return when {
        d.length == 11 && d.matches(Regex("01[016789]\\d+")) ->
            "${d.substring(0, 3)}-${d.substring(3, 7)}-${d.substring(7)}"
        d.length == 9  && d.startsWith("02") ->
            "${d.substring(0, 2)}-${d.substring(2, 5)}-${d.substring(5)}"
        d.length == 10 && d.startsWith("02") ->
            "${d.substring(0, 2)}-${d.substring(2, 6)}-${d.substring(6)}"
        d.length == 10 && d.startsWith("0") ->
            "${d.substring(0, 3)}-${d.substring(3, 6)}-${d.substring(6)}"
        d.length == 11 && d.startsWith("0") ->
            "${d.substring(0, 3)}-${d.substring(3, 7)}-${d.substring(7)}"
        d.length == 8  && d.matches(Regex("1[5-9]\\d+")) ->
            "${d.substring(0, 4)}-${d.substring(4)}"
        else -> raw
    }
}

@Composable
fun ContactCard(
    contact: ContactModel,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
) {
    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val hintC = if (isDark) AppColors.HintDark else AppColors.HintLight
    val badgeTx = if (isDark) Color(0xFFFFD966) else Color(0xFF7A5C00)
    val phoneC  = if (isDark) AppColors.BlueDark else AppColors.Blue
    val cardBg = if (isDark) AppColors.CardDark else AppColors.CardLight
    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val badgeBg = if (isDark) Color(0xFF2A2A3D) else Color(0xFFFFF8E0)
    val badgeBorder = Color(0xFFFAD02C).copy(alpha = if (isDark) 0.4f else 0.55f)

    val hasName = contact.name.isNotEmpty()
    val hasPhone = contact.phone.isNotEmpty()
    // 전화번호는 시스템 폰트 스케일 무관하게 고정 크기 유지 → 줄바꿈 방지
    val fixedPhoneSp = (14f / LocalDensity.current.fontScale).sp

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderC, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 11.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // 배지(왼쪽) + 전화번호(오른쪽) 같은 줄
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg,
                    modifier = Modifier.border(1.dp, badgeBorder, RoundedCornerShape(6.dp)),
                ) {
                    Text(
                        text = contact.role,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        color = badgeTx,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    tint = if (hasPhone) phoneC else hintC,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = if (hasPhone) fmtPhone(contact.phone) else "번호 없음",
                    fontSize = fixedPhoneSp,
                    fontWeight = if (hasPhone) FontWeight.W600 else FontWeight.Normal,
                    color = if (hasPhone) phoneC else hintC,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(6.dp))
            // 이름
            Text(
                text = if (hasName) contact.name else "미입력",
                fontSize = 19.sp,
                fontWeight = FontWeight.W700,
                color = if (hasName) textC else hintC,
            )
        }
    }
}
