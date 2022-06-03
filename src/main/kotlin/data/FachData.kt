package data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIncludeProperties

/**
 * Hält alle statischen Daten für die Kurswahl
 *
 * Daten werden aus der `dataStruct.json` gezogen
 *
 * Neue Felder müssen den selben Namen wie in der `dataStruct.json` haben,
 * mit dem Finalen Typen im Primary Constructor hinzugefügt und mit dem Eingelesenen Typ
 * (so wies in der JSON steht) im Secondary Constructor deklariert (u. ggf. umgeformt) werden.
 */
@JsonIncludeProperties(
    "faecher", "pflichtfaecher", "wpfs", "regeln", "wahlzeilen", "wildcards",
    "wzWildcards", "minKurse", "maxKurse", "pf3_4AusschlussFaecher"
)
data class FachData(
    val faecherMap: Map<String, Fach>,
    val pflichtfaecher: Map<Fach, Wahlmoeglichkeit>,
    val wpfs: List<Fach>,
    val regeln: List<Regel>,
    val wahlzeilen: Map<Int, Wahlzeile>,
    val wildcards: Map<String, List<Fach>>,
    val wzWildcards: Map<String, List<String>>,
    val minKurse: Int,
    val maxKurse: Int,
    val pf3_4AusschlussFaecher: Set<String>
) {
    @JsonCreator
    constructor(
        faecher: Map<String, Fach>,
        pflichtfaecher: Map<String, Wahlmoeglichkeit>,
        wpfs: List<String>,
        regeln: List<Regel>,
        wahlzeilen: Map<Int, Wahlzeile>,
        wildcards: Map<String, List<String>>,
        wzWildcards: List<String>,
        minKurse: Int,
        maxKurse: Int,
        pf3_4AusschlussFaecher: Set<String>
    ) : this(
        faecherMap = faecher,
        pflichtfaecher = pflichtfaecher.map { (k, v) -> faecher[k]!! to v }.toMap(),
        wpfs = wpfs.map { faecher[it]!! },
        regeln = regeln,
        wahlzeilen = wahlzeilen,
        wildcards = wildcards.mapValues { it.value.map { key -> faecher[key]!! } },
        wzWildcards = wzWildcards.associateWith { wildcards[it]!! },
        minKurse = minKurse,
        maxKurse = maxKurse,
        pf3_4AusschlussFaecher = pf3_4AusschlussFaecher
    )


    val faecher: List<Fach> = faecherMap.values.toList()

    val fremdsprachen = faecher.filter { it.fremdsprache }
    val lk1Moeglichkeiten = LinkedHashSet<String>().apply {
        for (wz in wahlzeilen.values) {
            if (wz.lk1.startsWith("$"))
                this.addAll(wzWildcards[wz.lk1]!!)
            else this.add(wz.lk1)
        }
    }.map { faecherMap[it]!! }
    val lk2Moeglichkeiten = LinkedHashSet<String>().apply {
        for (wz in wahlzeilen.values) {
            if (wz.lk2.startsWith("$"))
                this.addAll(wzWildcards[wz.lk2]!!)
            else this.add(wz.lk2)
        }
    }.map { faecherMap[it]!! }


    init {
        instanceHash = hashCode()
        regeln.forEach { it.fillData(this) }
    }


    /**
     * Gibt die die LKs zurück
     */
    val lks: List<Fach>
        get() = faecher.filter { it.lk }

    private val wildcardMapping =
        faecher.associateWith { wildcards.filter { wCard -> it in wCard.value }.keys + it.kuerzel }

    val wzWildcardMapping =
        faecher.associateWith { (wzWildcards.filterValues { value -> it.kuerzel in value }.keys + it.kuerzel) + "*" }

    /**
     * Gibt alle möglichen Wahlzeilen für die gegebenen LKs zurück
     */
    fun filterWahlzeilen(
        lk1: Fach?,
        lk2: Fach?,
        pf3: Fach? = null,
        pf4: Fach? = null,
        pf5: Fach? = null,
        wahlzeilen: Map<Int, Wahlzeile> = this.wahlzeilen
    ): Map<Int, Wahlzeile> {
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

    /*fun matchField(fach: Fach, selector: String): Boolean =
        selector == "*" || selector == fach.kuerzel || fach in wzWildcards[selector]!!*/

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
            "maxKurse=$maxKurse",
            "pf3_4AusschlussFaecher=$pf3_4AusschlussFaecher"
        ).joinToString(
            ",\n\t",
            "FachData(\n\t",
            "\n)"
        )

    companion object {
        /**
         * Hash der aktuell verwendeten JSON
         */
        var instanceHash: Int = -1
    }
}