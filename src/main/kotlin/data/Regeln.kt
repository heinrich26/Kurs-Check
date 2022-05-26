package data

data class Regeln(
    val regeln: List<RegelHolder>
) {
    fun toList() = regeln.map { it.regel }

    override fun toString(): String = "Regeln(regeln=${toList()})"
}
