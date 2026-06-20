package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.ContactCard
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.components.Emergency119Card
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import kotlinx.coroutines.delay

@Composable
fun ContactsScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    isDark: Boolean,
    onMenuClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()
    val locState by locationViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        locationViewModel.fetch()
        while (true) { delay(60_000); locationViewModel.fetch() }
    }

    val address = if (locState.locationEnabled && locState.status == LocationStatus.Ready) locState.address else null

    Scaffold(
        topBar = {
            DetailAppBar(
                title = "긴급 연락처",
                isDark = isDark,
                currentRoute = "contacts",
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Emergency119Card(isDark = isDark, address = address)
            Spacer(Modifier.height(8.dp))

            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val contacts = appData.contacts
                val spacing = 7.dp
                val density = LocalDensity.current

                // Measured natural card height in pixels (0 until first measurement)
                var naturalCardHeightPx by remember { mutableIntStateOf(0) }

                val estCardH = if (naturalCardHeightPx > 0)
                    with(density) { naturalCardHeightPx.toDp() }
                else
                    (93f * density.fontScale).dp
                val totalEst = estCardH * contacts.size + spacing * (contacts.size - 1).coerceAtLeast(0)
                val shouldScroll = contacts.isNotEmpty() && totalEst > maxHeight

                Column(
                    Modifier.fillMaxSize()
                        .then(if (shouldScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                ) {
                    contacts.forEachIndexed { i, c ->
                        if (i > 0) Spacer(Modifier.height(spacing))
                        ContactCard(
                            contact = c,
                            isDark = isDark,
                            modifier = if (shouldScroll) {
                                // Measure natural card height from the first card (only once)
                                if (i == 0 && naturalCardHeightPx == 0)
                                    Modifier.fillMaxWidth().onSizeChanged { naturalCardHeightPx = it.height }
                                else
                                    Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillMaxWidth().weight(1f)
                            },
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
