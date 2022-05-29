package data

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.*

/**
 * Hält alle statischen Daten für die Kurswahl
 *
 * Daten werden aus der `dataStruct.json` gezogen
 *
 * Neue Felder müssen den selben Namen wie in der `dataStruct.json` haben,
 * mit dem Finalen Typen im Primary Constructor hinzugefügt und mit dem Eingelesenen Typ
 * (so wies in der JSON steht) im Secondary Constructor deklariert (u. ggf. umgeformt) werden.
 */
class FachData(
    val faecherMap: Map<String, Fach>,
    val pflichtfaecher: Map<Fach, Wahlmoeglichkeit>,
    // val fremdsprachen: List<Fach>,
    val wpfs: List<Fach>,
    val regeln: List<Regel>,
    val wahlzeilen: Map<Int, Wahlzeile>,
    val wildcards: Map<String, List<Fach>>,
    val wzWildcards: Map<String, List<Fach>>,
    val minKurse: Int,
    val maxKurse: Int
    // val lk1Moeglichkeiten: List<Fach>
) {
    @JsonCreator
    constructor(
        faecher: Map<String, Fach>,
        pflichtfaecher: Map<String, Wahlmoeglichkeit>,
        /*fremdsprachen: List<String>,*/
        wpfs: List<String>,
        regeln: List<Regel>,
        wahlzeilen: Map<Int, Wahlzeile>,
        wildcards: Map<String, List<String>>,
        wzWildcards: List<String>,
        minKurse: Int,
        maxKurse: Int
    ) : this(
        faecherMap = faecher,
        pflichtfaecher = pflichtfaecher.map { (k, v) -> faecher[k]!! to v }.toMap(),
        //  fremdsprachen = faecher.values.filter { it.fremdsprache },
        wpfs = wpfs.map { faecher[it]!! },
        regeln = regeln,
        wahlzeilen = wahlzeilen,
        wildcards = wildcards.mapValues { it.value.map { key -> faecher[key]!! } },
        wzWildcards = wzWildcards.associateWith { wCard -> wildcards[wCard]!!.map { faecher[it]!! } },
        minKurse = minKurse,
        maxKurse = maxKurse
        /*lk1Moeglichkeiten = lk1Moeglichkeiten.map {
            if (it.startsWith("$")) wildcards[it]!!.map { kz -> faecher[kz]!! } else listOf(faecher[it]!!)
        }.flatten()*/
    )


    val faecher: List<Fach> = faecherMap.values.toList()

    val fremdsprachen = faecher.filter { it.fremdsprache }
    val lk1Moeglichkeiten = LinkedHashSet(wahlzeilen.values.flatMap { wz ->
        val kuerzel = wz.lk1
        if (kuerzel.startsWith("$")) wzWildcards[kuerzel]!!
        else Collections.singleton(faecherMap[kuerzel]!!)
    }.filter { it.lk })
    val lk2Moeglichkeiten = LinkedHashSet(wahlzeilen.values.flatMap { wz ->
        val kuerzel = wz.lk2
        if (kuerzel.startsWith("$")) wzWildcards[kuerzel]!!
        else Collections.singleton(faecherMap[kuerzel]!!)
    }.filter { it.lk })

    init {
        regeln.forEach { it.fillData(this) }
    }


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