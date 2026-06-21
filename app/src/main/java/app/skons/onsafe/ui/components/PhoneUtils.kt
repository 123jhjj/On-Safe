package app.skons.onsafe.ui.components

internal fun fmtPhone(raw: String): String {
    if (raw.contains('-')) return raw
    val d = raw.replace(Regex("\\D"), "")
    return when {
        d.length == 11 && d.matches(Regex("01[016789]\\d+")) ->
            "${d.substring(0, 3)}-${d.substring(3, 7)}-${d.substring(7)}"
        d.length == 9 && d.startsWith("02") ->
            "${d.substring(0, 2)}-${d.substring(2, 5)}-${d.substring(5)}"
        d.length == 10 && d.startsWith("02") ->
            "${d.substring(0, 2)}-${d.substring(2, 6)}-${d.substring(6)}"
        d.length == 10 && d.startsWith("0") ->
            "${d.substring(0, 3)}-${d.substring(3, 6)}-${d.substring(6)}"
        d.length == 11 && d.startsWith("0") ->
            "${d.substring(0, 3)}-${d.substring(3, 7)}-${d.substring(7)}"
        d.length == 8 && d.matches(Regex("1[5-9]\\d+")) ->
            "${d.substring(0, 4)}-${d.substring(4)}"
        else -> raw
    }
}
