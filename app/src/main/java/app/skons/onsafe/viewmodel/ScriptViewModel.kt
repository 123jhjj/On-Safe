package app.skons.onsafe.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.skons.onsafe.data.OnSafePreferences
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KEY_SCRIPT = "onsafe-script"
private const val TAG = "ScriptViewModel"

data class ScriptState(
    val company: String = "",
    val reporter: String = "",
    val time: String = "",
    val location: String = "",
    val workName: String = "",
    val victim: String = "",
    val incident: String = "",
    val status: String = "",
)

class ScriptViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScriptState())
    val state: StateFlow<ScriptState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val raw = OnSafePreferences.appPrefs(getApplication())
                    .getString(KEY_SCRIPT, null) ?: return@withContext
                try {
                    val d = JSONObject(raw)
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            company  = d.optString("company", ""),
                            reporter = d.optString("name", ""),
                            location = d.optString("location", ""),
                            workName = d.optString("workName", ""),
                            victim   = d.optString("victim", ""),
                            incident = d.optString("incident", ""),
                            status   = d.optString("status", ""),
                        )
                    }
                } catch (e: JSONException) {
                    Log.w(TAG, "Failed to parse stored script data", e)
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            val json = JSONObject().apply {
                put("company", s.company); put("name", s.reporter)
                put("location", s.location); put("workName", s.workName)
                put("victim", s.victim); put("incident", s.incident); put("status", s.status)
            }
            OnSafePreferences.appPrefs(getApplication())
                .edit().putString(KEY_SCRIPT, json.toString()).apply()
        }
    }

    fun update(transform: (ScriptState) -> ScriptState) {
        _state.value = transform(_state.value)
        save()
    }

    fun reset(company: String, reporter: String) {
        _state.value = ScriptState(company = company, reporter = reporter, time = nowStr())
        save()
    }

    fun initDefaults(company: String, reporter: String) {
        val s = _state.value
        _state.value = s.copy(
            company = company.ifEmpty { s.company },
            reporter = reporter.ifEmpty { s.reporter },
            time = if (s.time.isEmpty()) nowStr() else s.time,
        )
        save()
    }

    companion object {
        fun nowStr(): String =
            SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREAN).format(Date())
    }
}
