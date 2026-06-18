package app.skons.onsafe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    company: String,
    isDark: Boolean,
    currentRoute: String,
    onMenuClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "메뉴",
                            tint = AppColors.AppBarFg)
                    }
                    Spacer(Modifier.width(2.dp))
                    HomeTitle(company = company)
                }
            },
            navigationIcon = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.AppBarYellow,
                titleContentColor = AppColors.AppBarFg,
            ),
        )
        Divider(color = if (isDark) AppColors.BorderDark else AppColors.DividerLight, thickness = 1.dp)
        CategoryNavBar(
            currentRoute = currentRoute,
            isDark = isDark,
            onNavigate = onNavigate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAppBar(
    title: String,
    isDark: Boolean,
    currentRoute: String,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "뒤로",
                            tint = AppColors.AppBarFg)
                    }
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.AppBarFg,
                    )
                }
            },
            navigationIcon = {},
            actions = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "메뉴",
                        tint = AppColors.AppBarFg)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.AppBarYellow,
                titleContentColor = AppColors.AppBarFg,
            ),
        )
        Divider(color = if (isDark) AppColors.BorderDark else AppColors.DividerLight, thickness = 1.dp)
        CategoryNavBar(
            currentRoute = currentRoute,
            isDark = isDark,
            onNavigate = onNavigate,
        )
    }
}

@Composable
private fun HomeTitle(company: String) {
    androidx.compose.foundation.layout.Box {
        if (company.isNotEmpty()) {
            Text(
                text = company,
                fontSize = 13.sp,
                fontWeight = FontWeight.W600,
                color = AppColors.AppBarFg,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        Row {
            Text(
                text = "On-Safe",
                fontSize = 20.sp,
                fontWeight = FontWeight.W800,
                color = AppColors.AppBarFg,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.background(AppColors.AppBarYellow),
            )
            Spacer(Modifier.width(10.dp))
        }
    }
}
