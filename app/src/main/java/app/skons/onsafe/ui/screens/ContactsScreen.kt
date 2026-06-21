package app.skons.onsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.skons.onsafe.ui.components.ContactCard
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.components.Emergency119Card
import app.skons.onsafe.ui.components.LocationPeriodicFetch
import app.skons.onsafe.ui.navigateMain
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.LocalDarkTheme
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel

@Composable
fun ContactsScreen(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    onMenuClick: () -> Unit,
) {
    val isDark = LocalDarkTheme.current
    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()
    val locState by locationViewModel.state.collectAsStateWithLifecycle()

    LocationPeriodicFetch(locationViewModel)

    val address = if (locState.locationEnabled && locState.status == LocationStatus.Ready) locState.address else null
    val contacts = appData.contacts

    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val cardBg  = if (isDark) AppColors.CardDark   else AppColors.CardLight

    Scaffold(
        topBar = {
            DetailAppBar(
                title = "긴급 연락처",
                currentRoute = "contacts",
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
                .padding(horizontal = 14.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            Emergency119Card(address = address)

            if (contacts.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))

                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(bottom = 10.dp)) {
                    val density = LocalDensity.current
                    var naturalCardHeightPx by remember { mutableIntStateOf(0) }

                    val estCardH = if (naturalCardHeightPx > 0)
                        with(density) { naturalCardHeightPx.toDp() }
                    else
                        (70f * density.fontScale).dp

                    val totalEst = estCardH * contacts.size + 1.dp * (contacts.size - 1).coerceAtLeast(0)
                    val shouldScroll = totalEst > maxHeight

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .then(if (!shouldScroll) Modifier.fillMaxSize() else Modifier)
                            .border(1.dp, borderC, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .then(if (shouldScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                    ) {
                        contacts.forEachIndexed { i, contact ->
                            if (i > 0) HorizontalDivider(color = borderC, thickness = 1.dp)
                            ContactCard(
                                contact = contact,
                                modifier = when {
                                    !shouldScroll -> Modifier.fillMaxWidth().weight(1f)
                                    i == 0 && naturalCardHeightPx == 0 ->
                                        Modifier.fillMaxWidth().onSizeChanged { naturalCardHeightPx = it.height }
                                    else -> Modifier.fillMaxWidth()
                                },
                                onTap = {
                                    val phone = contact.phone.replace(Regex("\\D"), "")
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
}
