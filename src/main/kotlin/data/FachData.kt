package data

class FachData(
    val faecher: List<Fach>,
    val pflichtfaecher: Map<Fach, Wahlmoeglichkeit>,
    val fremdsprachen: List<Fach>,
    val wpfs: List<Fach>,
    val regeln: List<Regel>,
    val wahlzeilen: Map<Int, Wahlzeile>,
    val wildcards: Map<String, List<Fach>>,
    val wzWildcards: Map<String, List<Fach>>,
    val minKurse: Int,
    val maxKurse: Int
) {
    /**
     * Gibt die die LKs zurück
     */
    val lks: List<Fach>
        get() = faecher.filter { it.lk }

    private val wildcardMapping =
        faecher.associateWith { wildcards.filter { wCard -> it in wCard.value }.keys + it.kuerzel }

    private val wzWildcardMapping =
        faecher.associateWith { wzWildcards.filter { wCard -> it in wCard.value }.keys + it.kuerzel }

    /**
     * Gibt alle möglichen Wahlzeilen für die gegebenen LKs zurück
     */
    fun filterWahlzeilen(lk1: Fach?, lk2: Fach?, pf3: Fach?, pf4: Fach?, pf5: Fach?): Map<Int, Wahlzeile> {
        val predicates = mutableListOf<(Wahlzeile) -> Boolean>()
        if (lk1 != null) predicates.add { it.lk1 == "*" || it.lk1 in wzWildcardMapping[lk1]!! }
        if (lk2 != null) predicates.add { it.lk2 == "*" || it.lk2 in wzWildcardMapping[lk2]!! }
        if (pf3 != null) predicates.add { it.pf3 == "*" || it.pf3 in wzWildcardMapping[pf3]!! }
        if (pf4 != null) predicates.add { it.pf4 == "*" || it.pf4 in wzWildcardMapping[pf4]!! }
        if (pf5 != null) predicates.add { it.pf5 == "*" || it.pf5 in wzWildcardMapping[pf5]!! }

        if (predicates.isEmpty()) return wahlzeilen

        return wahlzeilen.filterValues { zeile -> predicates.all { it(zeile) } }
    }

    /**
     * Gibt alle möglichen Wahlzeilen für die gegebenen LKs zurück
     */
    fun filterWahlzeilen(data: KurswahlData): Map<Int, Wahlzeile> =
        filterWahlzeilen(data.lk1, data.lk2, data.pf3, data.pf4, data.pf5)

    @Deprecated("Ineffizient und brauchte ich erstmal nicht")
    fun matchField(fach: Fach, selector: String): Boolean =
        selector == "*" || selector == fach.kuerzel || fach in wzWildcards[selector]!!

    override fun toString(): String =
        arrayOf(
            "faecher=$faecher",
            "pflichtfaecher=$pflichtfaecher",
            "fremdsprachen=$fremdsprachen",
            "wpfs=$wpfs",
            "regeln=$regeln",
            "wahlzeilen=$wahlzeilen",
            "wildcards=$wildcards",
            "wzWildcards=$wzWildcards",
            "minKurse=$minKurse",
            "maxKurse=$maxKurse"
        ).joinToString(
            ",\n\t",
            "FachData(\n\t",
            "\n)"
        )
}