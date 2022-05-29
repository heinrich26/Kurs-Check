package data

data class Fach(
    val name: String,
    val kuerzel: String,
    val aufgabenfeld: Int,
    val lk: Boolean = false,
    val fremdsprache: Boolean = false,
    val brauchtWPF: Boolean = false
) {
    fun nameFormatted() = if (aufgabenfeld == 0) name else "$name ($aufgabenfeld)"
}
