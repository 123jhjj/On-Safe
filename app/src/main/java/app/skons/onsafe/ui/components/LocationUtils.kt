package app.skons.onsafe.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.skons.onsafe.viewmodel.LocationViewModel
import kotlinx.coroutines.delay

@Composable
fun LocationPeriodicFetch(locationViewModel: LocationViewModel) {
    LaunchedEffect(Unit) {
        locationViewModel.fetch()
        while (true) { delay(60_000); locationViewModel.fetch() }
    }
}
