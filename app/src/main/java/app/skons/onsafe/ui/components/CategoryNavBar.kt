package app.skons.onsafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.ui.theme.AppColors

private data class NavItem(val label: String, val icon: ImageVector, val route: String)

private val navItems = listOf(
    NavItem("사고 발생",   Icons.Outlined.Warning,      "accident"),
    NavItem("보고 체계",   Icons.Outlined.AccountTree,  "report"),
    NavItem("긴급 연락처", Icons.Outlined.PhoneInTalk,  "contacts"),
    NavItem("보고 양식",   Icons.Outlined.Assignment,   "script"),
)

@Composable
fun CategoryNavBar(
    currentRoute: String,
    isDark: Boolean,
    onNavigate: (String) -> Unit,
) {
    val activeColor = if (isDark) AppColors.RedDark else AppColors.Red
    val inactiveColor = if (isDark) AppColors.SubDark else AppColors.SubLight
    val bgColor = if (isDark) AppColors.CardDark else AppColors.CardLight

    Row(modifier = Modifier.background(bgColor)) {
        navItems.forEach { item ->
            val isActive = currentRoute == item.route
            val color = if (isActive) activeColor else inactiveColor

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 3.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (isActive) Modifier.background(activeColor.copy(alpha = 0.12f))
                        else Modifier
                    )
                    .clickable(enabled = !isActive) { onNavigate(item.route) }
                    .padding(vertical = 2.dp),
            ) {
                Icon(item.icon, contentDescription = item.label, tint = color,
                    modifier = Modifier.padding(bottom = 3.dp))
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    color = color,
                    fontWeight = if (isActive) FontWeight.W800 else FontWeight.W500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
