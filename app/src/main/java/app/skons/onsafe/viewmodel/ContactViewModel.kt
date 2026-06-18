package app.skons.onsafe.viewmodel

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.skons.onsafe.data.AppData
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.data.ContactRepository
import app.skons.onsafe.data.MyInfo
import app.skons.onsafe.widget.OnSafeWidget119
import app.skons.onsafe.widget.OnSafeWidgetContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()

    val myInfo get() = _data.value.myInfo
    val allContacts get() = _data.value.contacts

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _data.value = ContactRepository.load(application)
        }
    }

    private fun save() {
        val ctx = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            ContactRepository.save(ctx, _data.value)
            refreshWidgets()
        }
    }

    private fun refreshWidgets() {
        val ctx = getApplication<Application>()
        try {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids119 = mgr.getAppWidgetIds(ComponentName(ctx, OnSafeWidget119::class.java))
            val idsContact = mgr.getAppWidgetIds(ComponentName(ctx, OnSafeWidgetContact::class.java))
            if (ids119.isNotEmpty()) {
                val intent = android.content.Intent(ctx, OnSafeWidget119::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids119)
                }
                ctx.sendBroadcast(intent)
            }
            if (idsContact.isNotEmpty()) {
                val intent = android.content.Intent(ctx, OnSafeWidgetContact::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsContact)
                }
                ctx.sendBroadcast(intent)
            }
        } catch (e: RuntimeException) { /* ignore */ }
    }

    fun updateMyInfo(info: MyInfo) {
        _data.value = _data.value.copy(myInfo = info)
        save()
    }

    fun updateContact(id: String, name: String? = null, phone: String? = null) {
        _data.value = _data.value.copy(
            contacts = _data.value.contacts.map { c ->
                if (c.id == id) c.copy(
                    name = name ?: c.name,
                    phone = phone ?: c.phone,
                ) else c
            }
        )
        save()
    }

    fun updateExtraContact(id: String, role: String? = null, name: String? = null, phone: String? = null) {
        _data.value = _data.value.copy(
            contacts = _data.value.contacts.map { c ->
                if (c.id == id) c.copy(
                    role = role ?: c.role,
                    name = name ?: c.name,
                    phone = phone ?: c.phone,
                ) else c
            }
        )
        save()
    }

    fun addContact(role: String, name: String, phone: String) {
        val newContact = ContactModel(
            id = "extra-${System.currentTimeMillis()}",
            role = role,
            name = name,
            phone = phone,
            deletable = true,
        )
        _data.value = _data.value.copy(contacts = _data.value.contacts + newContact)
        save()
    }

    fun removeContact(id: String) {
        _data.value = _data.value.copy(
            contacts = _data.value.contacts.filter { it.id != id }
        )
        save()
    }

    fun reorderContacts(fromIndex: Int, toIndex: Int) {
        val list = _data.value.contacts.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _data.value = _data.value.copy(contacts = list)
        save()
    }
}
