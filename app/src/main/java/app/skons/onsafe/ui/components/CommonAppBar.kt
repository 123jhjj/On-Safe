package app.skons.onsafe.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.LocalDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    company: String,
    currentRoute: String,
    onMenuClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val isDark = LocalDarkTheme.current
    Column {
        TopAppBar(
            expandedHeight = 48.dp,
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "메뉴", tint = AppColors.AppBarFg,
                        modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text(
                    text = "On-Safe",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.AppBarFg,
                    letterSpacing = (-0.3).sp,
                )
            },
            actions = {
                if (company.isNotEmpty()) {
                    Text(
                        text = company,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.AppBarFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.AppBarYellow,
                titleContentColor = AppColors.AppBarFg,
            ),
        )
        HorizontalDivider(
            color = if (isDark) AppColors.BorderDark else AppColors.DividerLight,
            thickness = 1.dp,
        )
        CategoryNavBar(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAppBar(
    title: String,
    currentRoute: String,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val isDark = LocalDarkTheme.current
    Column {
        TopAppBar(
            expandedHeight = 48.dp,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "뒤로",
                        tint = AppColors.AppBarFg,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.AppBarFg,
                )
            },
            actions = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "메뉴", tint = AppColors.AppBarFg,
                        modifier = Modifier.size(28.dp))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.AppBarYellow,
                titleContentColor = AppColors.AppBarFg,
            ),
        )
        HorizontalDivider(
            color = if (isDark) AppColors.BorderDark else AppColors.DividerLight,
            thickness = 1.dp,
        )
        CategoryNavBar(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
        )
    }
}
