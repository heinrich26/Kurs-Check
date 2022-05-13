package data

data class Fach(val name: String, val kuerzel: String, val aufgabenfeld: Int?, val lk: Boolean) {
    fun nameFormatted() = if (aufgabenfeld == null) name else "$name ($aufgabenfeld)"
}
