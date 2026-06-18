package app.skons.onsafe.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.ui.theme.AppColors

@Composable
fun ContactCard(
    contact: ContactModel,
    isDark: Boolean,
    onTap: (() -> Unit)? = null,
) {
    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val hintC = if (isDark) AppColors.HintDark else AppColors.HintLight
    val badgeTx = if (isDark) Color(0xFFFFD966) else Color(0xFF7A5C00)
    val cardBg = if (isDark) AppColors.CardDark else AppColors.CardLight
    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val badgeBg = if (isDark) Color(0xFF2A2A3D) else Color(0xFFFFF8E0)
    val badgeBorder = Color(0xFFFAD02C).copy(alpha = if (isDark) 0.4f else 0.55f)

    val hasName = contact.name.isNotEmpty()
    val hasPhone = contact.phone.isNotEmpty()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderC, RoundedCornerShape(14.dp))
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier),
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 11.dp)) {
            // Role badge
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
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (hasName) contact.name else "미입력",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.W700,
                    color = if (hasName) textC else hintC,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = if (hasPhone) badgeTx else hintC,
                        modifier = Modifier.padding(end = 5.dp),
                    )
                    Text(
                        text = if (hasPhone) contact.phone else "번호 없음",
                        fontSize = 14.sp,
                        fontWeight = if (hasPhone) FontWeight.W600 else FontWeight.W400,
                        color = if (hasPhone) badgeTx else hintC,
                    )
                }
            }
        }
    }
}
