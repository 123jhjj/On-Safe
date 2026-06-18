package app.skons.onsafe.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val PREFS_NAME = "onsafe_prefs"
const val KEY_DATA = "onsafe-data"

private val defaultContacts = listOf(
    ContactModel(id = "site-manager",       role = "SKO 관리감독자",    deletable = false),
    ContactModel(id = "subcontract-safety", role = "SKO 총괄책임자", deletable = false),
    ContactModel(id = "skt-safety",         role = "SKT 안전관리자",       deletable = false),
    ContactModel(id = "skt-supervisor",     role = "SKT 관리감독자",       deletable = false),
)

data class AppData(
    val myInfo: MyInfo = MyInfo(),
    val contacts: List<ContactModel> = defaultContacts,
)

object ContactRepository {

    fun load(context: Context): AppData {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_DATA, null) ?: return AppData()
            val json = JSONObject(raw)
            val myInfoJson = json.optJSONObject("myInfo")
            val myInfo = if (myInfoJson != null) MyInfo(
                name = myInfoJson.optString("name", ""),
                siteName = myInfoJson.optString("siteName", ""),
                company = myInfoJson.optString("company", ""),
            ) else MyInfo()

            val contacts = mutableListOf<ContactModel>()
            val allArr = json.optJSONArray("allContacts")
            if (allArr != null) {
                for (i in 0 until allArr.length()) {
                    val c = allArr.optJSONObject(i) ?: continue
                    contacts.add(ContactModel(
                        id = c.optString("id", "extra-$i"),
                        role = c.optString("role", ""),
                        name = c.optString("name", ""),
                        phone = c.optString("phone", ""),
                        deletable = c.optBoolean("deletable", false),
                    ))
                }
            } else {
                contacts.addAll(defaultContacts)
            }
            AppData(myInfo = myInfo, contacts = contacts)
        } catch (e: JSONException) {
            AppData()
        }
    }

    fun save(context: Context, data: AppData) {
        val json = JSONObject().apply {
            put("myInfo", JSONObject().apply {
                put("name", data.myInfo.name)
                put("siteName", data.myInfo.siteName)
                put("company", data.myInfo.company)
            })
            put("allContacts", JSONArray().apply {
                data.contacts.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("role", c.role)
                        put("name", c.name)
                        put("phone", c.phone)
                        put("deletable", c.deletable)
                    })
                }
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_DATA, json.toString()).apply()
    }
}
