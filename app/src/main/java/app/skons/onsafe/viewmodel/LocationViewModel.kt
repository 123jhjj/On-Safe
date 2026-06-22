package app.skons.onsafe.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

enum class LocationStatus { Loading, Denied, ServiceDisabled, Failed, Ready }

private const val PREFS = "onsafe_prefs"
private const val KEY_LOCATION_ENABLED = "onsafe-location-enabled"

data class LocationState(
    val status: LocationStatus = LocationStatus.Loading,
    val address: String = "",
    val fetching: Boolean = false,
    val locationEnabled: Boolean = true,
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _state.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
    private var fetchJob: Job? = null
    private var locationCallback: LocationCallback? = null

    init {
        val prefs = application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_LOCATION_ENABLED, true)
        _state.value = _state.value.copy(locationEnabled = enabled)
    }

    fun setLocationEnabled(enabled: Boolean) {
        val ctx = getApplication<Application>()
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LOCATION_ENABLED, enabled).apply()
        _state.value = _state.value.copy(locationEnabled = enabled)
        if (enabled) fetch() else stopFetch()
    }

    fun fetch() {
        if (!_state.value.locationEnabled) return
        val ctx = getApplication<Application>()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _state.value = _state.value.copy(fetching = true, status = LocationStatus.Loading)
            fetchLocation(ctx)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocation(ctx: Context) {
        try {
            // Try last known first for quick display
            val lastKnown = withContext(Dispatchers.IO) {
                try {
                    var loc: android.location.Location? = null
                    val task = fusedClient.lastLocation
                    var done = false
                    task.addOnSuccessListener { l -> loc = l; done = true }
                    task.addOnFailureListener { done = true }
                    var waited = 0
                    while (!done && waited < 2000) { delay(50); waited += 50 }
                    loc
                } catch (e: SecurityException) { null }
                catch (e: IllegalStateException) { null }
                catch (e: RuntimeException) { null }
            }

            if (lastKnown != null && lastKnown.accuracy <= 50f) {
                val addr = reverseGeocode(ctx, lastKnown.latitude, lastKnown.longitude)
                if (addr.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        status = LocationStatus.Ready,
                        address = addr,
                        fetching = false,
                    )
                    return
                }
            }

            // Request fresh location
            var resolved = false
            val cb = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    if (resolved) return
                    val loc = result.lastLocation ?: return
                    resolved = true
                    fusedClient.removeLocationUpdates(this)
                    viewModelScope.launch {
                        val addr = reverseGeocode(ctx, loc.latitude, loc.longitude)
                        _state.value = _state.value.copy(
                            status = if (addr.isNotEmpty()) LocationStatus.Ready else LocationStatus.Failed,
                            address = addr,
                            fetching = false,
                        )
                    }
                }
            }
            locationCallback = cb
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMaxUpdates(1)
                .setWaitForAccurateLocation(false)
                .build()
            fusedClient.requestLocationUpdates(req, cb, Looper.getMainLooper())

            // Timeout after 8s
            delay(8000)
            if (!resolved) {
                fusedClient.removeLocationUpdates(cb)
                _state.value = _state.value.copy(
                    status = LocationStatus.Failed,
                    fetching = false,
                )
            }
        } catch (e: SecurityException) {
            _state.value = _state.value.copy(status = LocationStatus.Denied, fetching = false)
        } catch (e: IllegalStateException) {
            _state.value = _state.value.copy(status = LocationStatus.Failed, fetching = false)
        } catch (e: RuntimeException) {
            _state.value = _state.value.copy(status = LocationStatus.Failed, fetching = false)
        }
    }

    private fun cleanAddress(raw: String): String {
        var addr = raw.trim()
        if (addr.startsWith("대한민국")) addr = addr.removePrefix("대한민국").trimStart()
        addr = addr.replace(Regex("\\s*[\\(,].*"), "").trim()
        // 지번 "번지" 접미사 제거 ("3-7번지" → "3-7")
        addr = addr.replace(Regex("(\\d+(?:-\\d+)?)번지"), "$1")
        // 건물번호 뒤 건물명·층·호 제거 ("123 빌딩명" → "123", "3-7 2층 2호" → "3-7")
        addr = addr.replace(Regex("(\\d+(?:-\\d+)?)\\s+\\S.*$"), "$1").trim()
        return addr
    }

    private suspend fun reverseGeocode(ctx: Context, lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext ""
                @Suppress("DEPRECATION")
                val addresses = Geocoder(ctx, Locale.forLanguageTag("ko-KR"))
                    .getFromLocation(lat, lng, 3)
                if (!addresses.isNullOrEmpty()) {
                    cleanAddress(addresses[0].getAddressLine(0) ?: "")
                } else ""
            } catch (e: IOException) { "" }
        }

    private fun stopFetch() {
        fetchJob?.cancel()
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
        _state.value = _state.value.copy(fetching = false)
    }

    override fun onCleared() {
        super.onCleared()
        stopFetch()
    }
}
