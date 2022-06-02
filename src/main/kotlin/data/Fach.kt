package data

import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize(using = FachSerializer::class, keyUsing = FachKeySerializer::class)
data class Fach(
    val name: String,
    val kuerzel: String,
    val aufgabenfeld: Int,
    val lk: Boolean = false,
    val fremdsprache: Boolean = false,
    val brauchtWPF: Boolean = false
) {
    fun nameFormatted() = if (aufgabenfeld == 0) name else "$name ($aufgabenfeld)"

    override fun equals(other: Any?): Boolean = this === other || (other is Fach && this.kuerzel == other.kuerzel)
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + kuerzel.hashCode()
        result = 31 * result + aufgabenfeld
        result = 31 * result + lk.hashCode()
        result = 31 * result + fremdsprache.hashCode()
        result = 31 * result + brauchtWPF.hashCode()
        return result
    }
}
