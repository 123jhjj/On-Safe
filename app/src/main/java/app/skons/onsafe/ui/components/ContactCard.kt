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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.ui.theme.appThemeColors

@Composable
fun ContactCard(
    contact: ContactModel,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
) {
    val c = appThemeColors()
    val hasName = contact.name.isNotEmpty()
    val hasPhone = contact.phone.isNotEmpty()
    val fixedPhoneSp = (14f / LocalDensity.current.fontScale).sp

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = c.cardBg,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, c.border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 11.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = c.badgeBg,
                    modifier = Modifier.border(1.dp, c.badgeBorder, RoundedCornerShape(6.dp)),
                ) {
                    Text(
                        text = contact.role,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        color = c.badgeTx,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    tint = if (hasPhone) c.blue else c.hint,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = if (hasPhone) fmtPhone(contact.phone) else "번호 없음",
                    fontSize = fixedPhoneSp,
                    fontWeight = if (hasPhone) FontWeight.W600 else FontWeight.Normal,
                    color = if (hasPhone) c.blue else c.hint,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (hasName) contact.name else "미입력",
                fontSize = 19.sp,
                fontWeight = FontWeight.W700,
                color = if (hasName) c.text else c.hint,
            )
        }
    }
}
