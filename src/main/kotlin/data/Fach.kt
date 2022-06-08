package data

import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize(using = FachSerializer::class, keyUsing = FachKeySerializer::class)
data class Fach(
    val name: String,
    val kuerzel: String,
    val aufgabenfeld: Int,
    val lk: Boolean = false,
    val fremdsprache: Boolean = false,
    val brauchtWPF: Boolean = false,
    val nurPf4_5: Boolean = false
) {
    fun nameFormatted() = if (aufgabenfeld < 1) name else "$name ($aufgabenfeld)"

    override fun equals(other: Any?): Boolean = this === other || (other is Fach && this.kuerzel == other.kuerzel)

    override fun hashCode(): Int = kuerzel.hashCode() // nimmt an, dass das selbe Kürzel nur 1x vorkommt
}
