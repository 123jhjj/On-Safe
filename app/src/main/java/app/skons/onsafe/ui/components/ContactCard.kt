package app.skons.onsafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val hasPhone = contact.phone.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            if (contact.role.isNotEmpty()) {
                Text(contact.role, fontSize = 14.sp, color = c.sub, fontWeight = FontWeight.W500)
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = contact.name.ifEmpty { "미입력" },
                fontSize = 18.sp,
                fontWeight = FontWeight.W700,
                color = if (contact.name.isNotEmpty()) c.text else c.hint,
            )
        }
        Spacer(Modifier.width(12.dp))
        if (hasPhone) {
            Row(
                Modifier
                    .background(c.blue, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.PhoneInTalk, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text("전화", fontSize = 13.sp, fontWeight = FontWeight.W700, color = Color.White)
            }
        } else {
            Box(
                Modifier
                    .background(c.hint.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text("번호 없음", fontSize = 12.sp, color = c.hint, fontWeight = FontWeight.W500)
            }
        }
    }
}
