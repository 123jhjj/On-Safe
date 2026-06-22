package app.skons.onsafe.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import app.skons.onsafe.MainActivity
import app.skons.onsafe.R
import org.json.JSONException
import org.json.JSONObject

data class WidgetContact(val id: String, val role: String, val name: String, val phone: String)

object OnSafeWidgetLogic {
    const val WIDGET_PREFS = "onsafe_widget_prefs"
    const val EMERGENCY_ID = "__emergency_119__"
    private const val TAG = "OnSafeWidget"
    private const val APP_PREFS = "onsafe_prefs"
    private const val APP_KEY = "onsafe-data"
    val LAYOUT_ID = R.layout.widget_contact
    private const val COLOR_RED = "#E24B4A"
    private const val COLOR_ICON = "#185FA5"
    private const val COLOR_TEXT = "#1A1A2E"
    const val MIN_WIDTH_DP = 110
    const val MIN_HEIGHT_DP = 40
    private const val BASE_ICON_DP = 30
    private const val BASE_TEXT_SP = 14f
    private const val BASE_NAME_SP = 12f

    val EMERGENCY_CONTACT = WidgetContact(EMERGENCY_ID, "119 신고", "119", "119")

    val DEFAULT_ROLE_CONTACTS = listOf(
        WidgetContact("site-manager", "SKO 관리감독자", "", ""),
        WidgetContact("subcontract-safety", "SKO 총괄책임자", "", ""),
        WidgetContact("skt-safety", "SKT 안전관리자", "", ""),
        WidgetContact("skt-supervisor", "SKT 관리감독자", "", ""),
    )

    fun readContacts(context: Context): List<WidgetContact> {
        return try {
            val prefs = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(APP_KEY, null) ?: return emptyList()
            val json = JSONObject(raw)
            val list = mutableListOf<WidgetContact>()
            val arr = json.optJSONArray("allContacts")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val c = arr.optJSONObject(i) ?: continue
                    list.add(WidgetContact(
                        id = c.optString("id", "extra-$i"),
                        role = c.optString("role", ""),
                        name = c.optString("name", ""),
                        phone = c.optString("phone", ""),
                    ))
                }
            }
            list
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to read contacts", e)
            emptyList()
        }
    }

    fun buildSelectableContacts(context: Context): List<WidgetContact> {
        val stored = readContacts(context)
        val result = mutableListOf(EMERGENCY_CONTACT)
        stored.filter { it.id != EMERGENCY_ID }.forEach { result.add(it) }
        return result
    }

    fun transparencyPercentToAlpha(percent: Int): Int =
        ((100 - percent.coerceIn(0, 100)) * 255 / 100).coerceIn(0, 255)

    fun alphaToTransparencyPercent(alpha: Int): Int =
        (100 - (alpha.coerceIn(0, 255) * 100 / 255)).coerceIn(0, 100)

    fun saveContactId(context: Context, widgetId: Int, contactId: String) {
        context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
            .edit().putString("sel_$widgetId", contactId).apply()
    }

    fun getSavedContactId(context: Context, widgetId: Int): String? =
        context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
            .getString("sel_$widgetId", null)

    fun getSelectedContact(context: Context, widgetId: Int, all: List<WidgetContact>): WidgetContact {
        val savedId = getSavedContactId(context, widgetId)
        if (savedId == EMERGENCY_ID) return EMERGENCY_CONTACT
        if (savedId != null) {
            buildSelectableContacts(context).firstOrNull { it.id == savedId }?.let { return it }
            all.firstOrNull { it.id == savedId }?.let { return it }
        }
        if (isEmergencyWidgetId(context, widgetId)) return EMERGENCY_CONTACT
        return buildSelectableContacts(context).firstOrNull { it.id == "site-manager" }
            ?: DEFAULT_ROLE_CONTACTS.first()
    }

    fun getAlpha(context: Context, widgetId: Int): Int =
        context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
            .getInt("alpha_$widgetId", 220)

    fun saveAlpha(context: Context, widgetId: Int, alpha: Int) {
        context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
            .edit().putInt("alpha_$widgetId", alpha.coerceIn(0, 255)).apply()
    }

    fun widgetWidthDp(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int): Int {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val widthPx = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        return if (widthPx > 0) (widthPx / context.resources.displayMetrics.density).toInt()
        else MIN_WIDTH_DP
    }

    fun widgetHeightDp(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int): Int {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val heightPx = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        return if (heightPx > 0) (heightPx / context.resources.displayMetrics.density).toInt()
        else MIN_HEIGHT_DP
    }

    private fun sizeScale(widthDp: Int, heightDp: Int): Float {
        val w = widthDp.coerceAtLeast(MIN_WIDTH_DP).toFloat() / MIN_WIDTH_DP
        val h = heightDp.coerceAtLeast(MIN_HEIGHT_DP).toFloat() / MIN_HEIGHT_DP
        return maxOf(w, h).coerceIn(1f, 2.5f)
    }

    fun scaleTextSp(widthDp: Int, heightDp: Int): Float =
        (BASE_TEXT_SP * sizeScale(widthDp, heightDp)).coerceIn(12f, 22f)

    fun scaleNameSp(widthDp: Int, heightDp: Int): Float =
        (BASE_NAME_SP * sizeScale(widthDp, heightDp)).coerceIn(10f, 18f)

    fun scaleIconDp(widthDp: Int, heightDp: Int): Int =
        (BASE_ICON_DP * sizeScale(widthDp, heightDp)).toInt().coerceIn(22, 56)

    fun makeDialIntent(context: Context, phone: String, reqCode: Int): PendingIntent? {
        val clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.isEmpty()) return null
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(
            context, reqCode,
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            flags,
        )
    }

    fun makeLaunchIntent(context: Context, reqCode: Int): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(
            context, reqCode,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            flags,
        )
    }

    fun isEmergencyWidgetId(context: Context, appWidgetId: Int): Boolean {
        val am = AppWidgetManager.getInstance(context)
        val info = am.getAppWidgetInfo(appWidgetId)
        if (info != null) return info.provider.className.endsWith("OnSafeWidget119")
        val ids119 = am.getAppWidgetIds(ComponentName(context, OnSafeWidget119::class.java))
        return appWidgetId in ids119
    }

    fun refreshWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        if (isEmergencyWidgetId(context, appWidgetId)) {
            OnSafeWidget119().refreshOne(context, appWidgetManager, appWidgetId)
        } else {
            OnSafeWidgetContact().refreshOne(context, appWidgetManager, appWidgetId)
        }
    }

    fun refreshAllWidgets(context: Context) {
        val am = AppWidgetManager.getInstance(context)
        val w119 = OnSafeWidget119()
        val wContact = OnSafeWidgetContact()
        am.getAppWidgetIds(ComponentName(context, OnSafeWidget119::class.java))
            .forEach { w119.refreshOne(context, am, it) }
        am.getAppWidgetIds(ComponentName(context, OnSafeWidgetContact::class.java))
            .forEach { wContact.refreshOne(context, am, it) }
    }

    fun populateViews(
        context: Context,
        views: RemoteViews,
        contact: WidgetContact,
        widgetId: Int,
        widthDp: Int,
        heightDp: Int,
    ) {
        val isEmergency = contact.id == EMERGENCY_ID
        val bg = if (isEmergency) Color.parseColor(COLOR_RED)
        else Color.argb(getAlpha(context, widgetId), 255, 255, 255)
        try { views.setInt(R.id.widget_root, "setBackgroundColor", bg) } catch (_: IllegalArgumentException) {}

        val iconColor = if (isEmergency) Color.WHITE else Color.parseColor(COLOR_ICON)
        try { views.setInt(R.id.contact1_call_icon, "setColorFilter", iconColor) } catch (_: IllegalArgumentException) {}

        val iconDp = scaleIconDp(widthDp, heightDp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                views.setViewLayoutWidth(R.id.contact1_call_icon, iconDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
                views.setViewLayoutHeight(R.id.contact1_call_icon, iconDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
            } catch (_: IllegalArgumentException) {}
        }

        try { views.setTextViewText(R.id.contact1_role, contact.role.takeIf { it.isNotBlank() } ?: "연락처") } catch (_: IllegalArgumentException) {}
        try { views.setTextViewTextSize(R.id.contact1_role, TypedValue.COMPLEX_UNIT_SP, scaleTextSp(widthDp, heightDp)) } catch (_: IllegalArgumentException) {}
        val textColor = if (isEmergency) Color.WHITE else Color.parseColor(COLOR_TEXT)
        try { views.setTextColor(R.id.contact1_role, textColor) } catch (_: IllegalArgumentException) {}

        val nameText = if (!isEmergency) contact.name.takeIf { it.isNotBlank() } else null
        try {
            if (nameText != null) {
                views.setViewVisibility(R.id.contact1_name, View.VISIBLE)
                views.setTextViewText(R.id.contact1_name, nameText)
                views.setTextViewTextSize(R.id.contact1_name, TypedValue.COMPLEX_UNIT_SP, scaleNameSp(widthDp, heightDp))
                views.setTextColor(R.id.contact1_name, Color.parseColor("#5A5A7A"))
            } else {
                views.setViewVisibility(R.id.contact1_name, View.GONE)
            }
        } catch (_: IllegalArgumentException) {}

        val phone = contact.phone.replace(Regex("[^0-9+]"), "")
        val pending = if (phone.isNotEmpty()) makeDialIntent(context, phone, widgetId) ?: makeLaunchIntent(context, widgetId)
        else makeLaunchIntent(context, widgetId)
        try { views.setOnClickPendingIntent(R.id.contact1_container, pending) } catch (_: IllegalArgumentException) {}
    }

    fun applyPreviewToView(context: Context, root: android.view.View, contact: WidgetContact, transparencyPercent: Int) {
        val widgetRoot = root.findViewById<android.view.View>(R.id.widget_root) ?: return
        val icon = root.findViewById<android.widget.ImageView>(R.id.contact1_call_icon)
        val roleTv = root.findViewById<android.widget.TextView>(R.id.contact1_role)
        val isEmergency = contact.id == EMERGENCY_ID

        val bg = if (isEmergency) Color.parseColor(COLOR_RED)
        else Color.argb(transparencyPercentToAlpha(transparencyPercent), 255, 255, 255)
        widgetRoot.setBackgroundColor(bg)

        val iconColor = if (isEmergency) Color.WHITE else Color.parseColor(COLOR_ICON)
        icon?.setColorFilter(iconColor, android.graphics.PorterDuff.Mode.SRC_IN)

        roleTv?.text = contact.role.takeIf { it.isNotBlank() } ?: "연락처"
        roleTv?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        roleTv?.setTextColor(if (isEmergency) Color.WHITE else Color.parseColor(COLOR_TEXT))
    }
}

abstract class OnSafeWidgetBase : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { refreshOne(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        refreshOne(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val edit = context.getSharedPreferences(OnSafeWidgetLogic.WIDGET_PREFS, Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { id -> edit.remove("sel_$id"); edit.remove("alpha_$id") }
        edit.apply()
    }

    fun refreshOne(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val contacts = OnSafeWidgetLogic.readContacts(context)
            val contact = OnSafeWidgetLogic.getSelectedContact(context, appWidgetId, contacts)
            val widthDp = OnSafeWidgetLogic.widgetWidthDp(context, appWidgetManager, appWidgetId)
            val heightDp = OnSafeWidgetLogic.widgetHeightDp(context, appWidgetManager, appWidgetId)
            val views = RemoteViews(context.packageName, OnSafeWidgetLogic.LAYOUT_ID)
            OnSafeWidgetLogic.populateViews(context, views, contact, appWidgetId, widthDp, heightDp)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Resources.NotFoundException) {
            Log.e("OnSafeWidget", "refresh failed for $appWidgetId", e)
        } catch (e: IllegalArgumentException) {
            Log.e("OnSafeWidget", "refresh failed for $appWidgetId", e)
        } catch (e: RuntimeException) {
            Log.e("OnSafeWidget", "refresh failed for $appWidgetId", e)
        }
    }
}

class OnSafeWidget119 : OnSafeWidgetBase()

class OnSafeWidgetContact : OnSafeWidgetBase()
