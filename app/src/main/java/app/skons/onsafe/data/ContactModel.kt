package app.skons.onsafe.data

data class ContactModel(
    val id: String,
    val role: String,
    val name: String = "",
    val phone: String = "",
    val deletable: Boolean = false,
)

data class MyInfo(
    val name: String = "",
    val company: String = "",
)
