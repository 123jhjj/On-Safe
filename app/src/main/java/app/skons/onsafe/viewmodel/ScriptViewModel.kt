package app.skons.onsafe.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar

private const val PREFS_SCRIPT = "onsafe_prefs"
private const val KEY_SCRIPT = "onsafe-script"

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
                val raw = getApplication<Application>()
                    .getSharedPreferences(PREFS_SCRIPT, Context.MODE_PRIVATE)
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
                } catch (_: JSONException) {}
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
            getApplication<Application>()
                .getSharedPreferences(PREFS_SCRIPT, Context.MODE_PRIVATE)
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
        if (_state.value.time.isEmpty()) {
            _state.value = _state.value.copy(
                company = _state.value.company.ifEmpty { company },
                reporter = _state.value.reporter.ifEmpty { reporter },
                time = nowStr(),
            )
        }
    }

    companion object {
        fun nowStr(): String {
            val c = Calendar.getInstance()
            return "${c.get(Calendar.YEAR)}년 ${
                (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}월 ${
                c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}일 ${
                c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${
                c.get(Calendar.MINUTE).toString().padStart(2, '0')}"
        }
    }
}
