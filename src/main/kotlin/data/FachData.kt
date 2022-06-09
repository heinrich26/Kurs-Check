package data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.IOException

/**
 * Hält alle statischen Daten für die Kurswahl
 *
 * Daten werden aus der `dataStruct.json` gezogen
 *
 * Neue Felder müssen den selben Namen wie in der `dataStruct.json` haben,
 * mit dem Finalen Typen im Primary Constructor hinzugefügt und mit dem Eingelesenen Typ
 * (so wies in der JSON steht) im Secondary Constructor deklariert (u. ggf. umgeformt) werden.
 */
@Suppress("unused")
@JsonIncludeProperties(
    "faecher", "pflichtfaecher", "wpfs", "regeln", "wahlzeilen", "wildcards",
    "wzWildcards", "minKurse", "maxKurse", "pf3_4AusschlussFaecher", "jsonVersion"
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
        // Regeln initialisieren
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
        wahlzeilen: Map<Int, Wahlzeile> = this.wahlzeilen
    ): Map<Int, Wahlzeile> {
        val predicates = mutableListOf<(Wahlzeile) -> Boolean>()
        if (lk1 != null) predicates.add { it.lk1 == "*" || it.lk1 in wzWildcardMapping[lk1]!! }
        if (lk2 != null) predicates.add { it.lk2 == "*" || it.lk2 in wzWildcardMapping[lk2]!! }

        if (predicates.isEmpty()) return wahlzeilen

        return wahlzeilen.filterValues { zeile -> predicates.all { it(zeile) } }
    }


    /**
     * Läd eine Kurswahl-Datei mit der in [gui.Consts] definierten Endung
     */
    @Throws(IOException::class, StreamReadException::class, DatabindException::class)
    fun loadKurswahl(file: File): KurswahlData {
        val mapper = jacksonObjectMapper()
        val injectables = InjectableValues.Std()
        injectables.addValue(FachData::class.java, this)
        mapper.injectableValues = injectables
        return mapper.readValue(file, KurswahlData::class.java)
    }

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
            "FachData[version ${jsonVersion.first}.${jsonVersion.second}](\n\t",
            "\n)"
        )


    companion object {
        /**
         * (major-, sub-) Version der aktuell verwendeten JSON
         */
        var jsonVersion: Pair<Int, Int> = 0 to 0

        @JvmStatic
        @JsonCreator
        fun fromJson(
            @JsonDeserialize(using = VersionDeserializer::class) @JsonProperty jsonVersion: Pair<Int, Int>,
            @JsonProperty faecher: List<Fach>,
            @JsonProperty pflichtfaecher: Map<String, Wahlmoeglichkeit>,
            @JsonProperty wpfs: List<String>,
            @JsonProperty regeln: List<Regel>,
            @JsonProperty wahlzeilen: Map<Int, Wahlzeile>,
            @JsonProperty wildcards: Map<String, List<String>>,
            @JsonProperty wzWildcards: List<String>,
            @JsonProperty minKurse: Int,
            @JsonProperty maxKurse: Int,
            @JsonProperty pf3_4AusschlussFaecher: Set<String>
        ): FachData {
            // jsonVersion global setzen
            FachData.jsonVersion = jsonVersion

            // Fächer zusätzlich sortieren um auf Aufgabenfelder aufzuteilen
            val faecherMap: Map<String, Fach> = faecher.sortedBy { if (it.aufgabenfeld > 0) it.aufgabenfeld else 4 }.associateBy { it.kuerzel }
            return FachData(
                faecherMap = faecherMap,
                pflichtfaecher = pflichtfaecher.mapKeys { (key: String) -> faecherMap[key]!! },
                wpfs = wpfs.map { faecherMap[it]!! },
                regeln = regeln,
                wahlzeilen = wahlzeilen,
                wildcards = wildcards.mapValues { it.value.map { key -> faecherMap[key]!! } },
                wzWildcards = wzWildcards.associateWith { wildcards[it]!! },
                minKurse = minKurse,
                maxKurse = maxKurse,
                pf3_4AusschlussFaecher = pf3_4AusschlussFaecher
            )
        }
    }

    /* Außer verwendung, denn Hashcodes sind Buildabhängig und deswegen nicht als
    Versionsindikator tauglich */
    /*override fun hashCode(): Int {
        var result = faecher.toSet().hashCode()
        result = 31 * result + pflichtfaecher.hashCode()
        result = 31 * result + fremdsprachen.toSet().hashCode()
        result = 31 * result + wpfs.toSet().hashCode()
        result = 31 * result + regeln.toSet().hashCode() // ^ gehen
        result = 31 * result + wahlzeilen.hashCode()
        result = 31 * result + wildcards.mapValues { it.value.toSet() }.hashCode()
        result = 31 * result + wzWildcards.mapValues { it.value.toSet() }.hashCode()
        result = 31 * result + minKurse.hashCode()
        result = 31 * result + maxKurse.hashCode()
        result = 31 * result + pf3_4AusschlussFaecher.hashCode()

        return result
    }*/
}