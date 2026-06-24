package app.skons.onsafe.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

private const val TAG = "OnSafePreferences"
private const val LEGACY_PREFS_NAME = "onsafe_prefs"
private const val ENCRYPTED_PREFS_NAME = "onsafe_prefs_secure"
private const val MIGRATION_FLAG = "_encrypted_migration_v1"

/** App data preferences (contacts, scripts, settings). Encrypted at rest. */
object OnSafePreferences {

    @Volatile
    private var cachedAppPrefs: SharedPreferences? = null

    fun appPrefs(context: Context): SharedPreferences {
        cachedAppPrefs?.let { return it }
        synchronized(this) {
            cachedAppPrefs?.let { return it }
            val prefs = openAppPrefs(context.applicationContext)
            cachedAppPrefs = prefs
            return prefs
        }
    }

    private fun openAppPrefs(context: Context): SharedPreferences {
        val legacySnapshot = snapshotLegacyPrefs(context)
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val encrypted = EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            if (!encrypted.getBoolean(MIGRATION_FLAG, false)) {
                migrateSnapshot(encrypted, legacySnapshot)
                encrypted.edit().putBoolean(MIGRATION_FLAG, true).apply()
                context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().clear().apply()
            }
            return encrypted
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Encrypted prefs unavailable", e)
            throw IllegalStateException("Encrypted prefs unavailable", e)
        } catch (e: IOException) {
            Log.e(TAG, "Encrypted prefs unavailable", e)
            throw IllegalStateException("Encrypted prefs unavailable", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Encrypted prefs unavailable", e)
            throw IllegalStateException("Encrypted prefs unavailable", e)
        }
    }

    private fun snapshotLegacyPrefs(context: Context): Map<String, Any?> {
        val legacy = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        return legacy.all.filterValues { it != null }
    }

    private fun migrateSnapshot(target: SharedPreferences, snapshot: Map<String, Any?>) {
        if (snapshot.isEmpty()) return
        val edit = target.edit()
        for ((key, value) in snapshot) {
            when (value) {
                is String -> edit.putString(key, value)
                is Int -> edit.putInt(key, value)
                is Long -> edit.putLong(key, value)
                is Float -> edit.putFloat(key, value)
                is Boolean -> edit.putBoolean(key, value)
                else -> Log.w(TAG, "Skipped unsupported pref type for key=$key")
            }
        }
        edit.apply()
    }
}
