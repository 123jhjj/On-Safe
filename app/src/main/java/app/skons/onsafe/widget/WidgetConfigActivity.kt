package app.skons.onsafe.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import app.skons.onsafe.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class WidgetConfigActivity : Activity() {

    companion object {
        private const val TAG = "WidgetConfig"
    }

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectableTargets = listOf<WidgetContact>()
    private var selectedContactIdx = 0
    private var transparencyPercent = 13

    private lateinit var tvContactRole: TextView
    private lateinit var tvContactName: TextView
    private lateinit var tvTransparencyValue: TextView
    private lateinit var seekTransparency: SeekBar
    private lateinit var sectionAlpha: View
    private var contactSheet: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_widget_config)
        applySystemBars()

        tvContactRole = findViewById(R.id.tv_contact_role)
        tvContactName = findViewById(R.id.tv_contact_name)
        tvTransparencyValue = findViewById(R.id.tv_transparency_value)
        seekTransparency = findViewById(R.id.seek_transparency)
        sectionAlpha = findViewById(R.id.section_alpha)

        reloadContacts()
        setupTransparencySlider()

        findViewById<View>(R.id.contact_selector).setOnClickListener { showContactBottomSheet() }
        findViewById<Button>(R.id.btn_cancel).setOnClickListener { setResult(RESULT_CANCELED); finish() }
        findViewById<Button>(R.id.btn_confirm).setOnClickListener { saveAndFinish() }
    }

    override fun onResume() {
        super.onResume()
        val prevId = selectableTargets.getOrNull(selectedContactIdx)?.id
        reloadContacts()
        if (prevId != null) {
            val idx = selectableTargets.indexOfFirst { it.id == prevId }
            if (idx >= 0) selectedContactIdx = idx
        }
        updateContactDisplay()
        updateTransparencyEnabled()
    }

    private fun setupTransparencySlider() {
        val savedAlpha = OnSafeWidgetLogic.getAlpha(this, appWidgetId)
        transparencyPercent = OnSafeWidgetLogic.alphaToTransparencyPercent(savedAlpha)
        seekTransparency.progress = transparencyPercent
        updateTransparencyLabel()

        seekTransparency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                transparencyPercent = progress
                updateTransparencyLabel()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateTransparencyLabel() {
        tvTransparencyValue.text = getString(R.string.widget_config_opacity_value, transparencyPercent)
    }

    private fun is119Selected(): Boolean =
        selectableTargets.getOrNull(selectedContactIdx)?.id == OnSafeWidgetLogic.EMERGENCY_ID

    private fun updateTransparencyEnabled() {
        val enabled = !is119Selected()
        seekTransparency.isEnabled = enabled
        sectionAlpha.alpha = if (enabled) 1f else 0.45f
    }

    private fun reloadContacts() {
        selectableTargets = OnSafeWidgetLogic.buildSelectableContacts(this)
        val savedId = OnSafeWidgetLogic.getSavedContactId(this, appWidgetId)
        selectedContactIdx = when {
            savedId != null -> selectableTargets.indexOfFirst { it.id == savedId }.takeIf { it >= 0 } ?: 0
            OnSafeWidgetLogic.isEmergencyWidgetId(this, appWidgetId) ->
                selectableTargets.indexOfFirst { it.id == OnSafeWidgetLogic.EMERGENCY_ID }.takeIf { it >= 0 } ?: 0
            else -> selectableTargets.indexOfFirst { it.id == "site-manager" }.takeIf { it >= 0 } ?: 0
        }
        updateContactDisplay()
        updateTransparencyEnabled()
    }

    private fun updateContactDisplay() {
        val c = selectableTargets.getOrNull(selectedContactIdx) ?: return
        tvContactRole.text = c.role.ifBlank { "연락처" }
        if (c.id == OnSafeWidgetLogic.EMERGENCY_ID) {
            tvContactName.text = "안전신고센터"
            tvContactName.setTextColor(color(R.color.widget_config_text_secondary))
        } else {
            tvContactName.text = c.name.takeIf { it.isNotBlank() } ?: getString(R.string.widget_config_unregistered)
            tvContactName.setTextColor(color(R.color.widget_config_text_tertiary))
        }
    }

    private fun color(resId: Int): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getColor(resId)
        else @Suppress("DEPRECATION") resources.getColor(resId)

    private fun applySystemBars() {
        val yellow = color(R.color.widget_config_header_bg)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = yellow
        window.navigationBarColor = color(R.color.widget_config_surface)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.decorView.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.decorView.systemUiVisibility = flags
        }
    }

    private fun showContactBottomSheet() {
        val sheet = BottomSheetDialog(this, R.style.Theme_OnSafe_BottomSheet)
        contactSheet = sheet
        val content = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_contact_picker, null)
        val container = content.findViewById<LinearLayout>(R.id.contact_list_container)
        container.removeAllViews()

        selectableTargets.forEachIndexed { index, contact ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_contact_picker, container, false)
            val root = row.findViewById<View>(R.id.contact_item_root)
            val roleTv = row.findViewById<TextView>(R.id.tv_item_role)
            val nameTv = row.findViewById<TextView>(R.id.tv_item_name)
            val check = row.findViewById<ImageView>(R.id.iv_item_check)

            roleTv.text = contact.role.ifBlank { "연락처" }
            nameTv.text = when {
                contact.id == OnSafeWidgetLogic.EMERGENCY_ID -> "안전신고센터 · 119"
                contact.name.isNotBlank() -> contact.name
                else -> getString(R.string.widget_config_unregistered)
            }

            val selected = index == selectedContactIdx
            root.setBackgroundResource(if (selected) R.drawable.bg_contact_item_selected else R.drawable.bg_contact_item)
            check.visibility = if (selected) View.VISIBLE else View.GONE
            roleTv.setTextColor(color(if (selected) R.color.widget_config_accent_dark else R.color.widget_config_text_primary))

            root.setOnClickListener {
                selectedContactIdx = index
                updateContactDisplay()
                updateTransparencyEnabled()
                sheet.dismiss()
                contactSheet = null
            }
            container.addView(row)
        }

        sheet.setContentView(content)
        sheet.show()
    }

    private fun saveAndFinish() {
        try {
            val contact = selectableTargets.getOrNull(selectedContactIdx)
            if (contact != null) OnSafeWidgetLogic.saveContactId(this, appWidgetId, contact.id)
            if (!is119Selected()) {
                OnSafeWidgetLogic.saveAlpha(this, appWidgetId, OnSafeWidgetLogic.transparencyPercentToAlpha(transparencyPercent))
            }
            OnSafeWidgetLogic.refreshWidget(this, AppWidgetManager.getInstance(this), appWidgetId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "saveAndFinish failed", e)
            setResult(RESULT_CANCELED)
            finish()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "saveAndFinish failed", e)
            setResult(RESULT_CANCELED)
            finish()
        } catch (e: RuntimeException) {
            Log.e(TAG, "saveAndFinish failed", e)
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
