package data

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonAppend
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * Beinhaltet die Daten der Kurswahl
 *
 * Sollte niemals vor [FachData] ohne [KurswahlData.readJsonVersion] erstellt werden
 */
@JsonAppend(attrs = [JsonAppend.Attr(value = "jsonVersion")], prepend = true)
@JsonIncludeProperties(
    "jsonVersion",
    "lk1",
    "lk2",
    "pf3",
    "pf4",
    "pf5",
    "pf5_typ",
    "gks",
    "fremdsprachen",
    "wpfs",
    "wahlzeile"
)
data class KurswahlData(
    var lk1: Fach? = null,
    var lk2: Fach? = null,
    var pf3: Fach? = null,
    var pf4: Fach? = null,
    var pf5: Fach? = null,
    var pf5_typ: Pf5Typ = Pf5Typ.PRAESENTATION,
    var gks: Map<Fach, Wahlmoeglichkeit> = emptyMap(),
    @get:JsonSerialize(using = ListOfPairSerializer::class) var fremdsprachen: List<Pair<Fach, Int>> = emptyList(),
    var wpfs: Pair<Fach, Fach?>? = null,
    var wahlzeile: Int = -1,
    /**
     * Version der bei der Erstellung verwendeten [FachData], um einen Mix von Formaten vorzubeugen
     */
    val readJsonVersion: Pair<Int, Int> = FachData.jsonVersion
) {

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(
            @JsonProperty @JsonDeserialize(using = VersionDeserializer::class) jsonVersion: Pair<Int, Int>,
            @JsonProperty lk1: String?,
            @JsonProperty lk2: String?,
            @JsonProperty pf3: String?,
            @JsonProperty pf4: String?,
            @JsonProperty pf5: String?,
            @JsonProperty pf5_typ: Pf5Typ,
            @JsonProperty gks: Map<String, Wahlmoeglichkeit>,
            @JsonProperty fremdsprachen: Map<String, Int>,
            @JsonProperty wpfs: Pair<String, String?>?,
            @JsonProperty wahlzeile: Int,
            @JacksonInject fachData: FachData
        ): KurswahlData = KurswahlData(
            lk1 = fachData.faecherMap[lk1].let { if (it != null && it.lk) it else null },
            lk2 = fachData.faecherMap[lk2].let { if (it != null && it.lk) it else null },
            pf3 = fachData.faecherMap[pf3],
            pf4 = fachData.faecherMap[pf4],
            pf5 = fachData.faecherMap[pf5],
            pf5_typ = pf5_typ,
            gks = gks.mapKeys { fachData.faecherMap[it.key]!! },
            fremdsprachen = fremdsprachen.map { fachData.faecherMap[it.key]!! to it.value },
            wpfs = if (wpfs == null || wpfs.first !in fachData.faecherMap.keys) null else fachData.faecherMap[wpfs.first]!! to fachData.faecherMap[wpfs.second],
            wahlzeile = wahlzeile,
            readJsonVersion = jsonVersion
        )
    }

    /**
     * Zählt die gewählten Kurse pro Semester
     */
    fun countCourses(): Array<Int> {
        val courseCounts = arrayOf(0, 0, 0, 0)
        for ((_, moegl) in kurse) {
            when (moegl) {
                Wahlmoeglichkeit.ERSTES_ZWEITES -> {
                    courseCounts[0]++
                    courseCounts[1]++
                }
                Wahlmoeglichkeit.ERSTES_DRITTES -> {
                    courseCounts[0]++
                    courseCounts[1]++
                    courseCounts[2]++
                }
                Wahlmoeglichkeit.ZWEITES_VIERTES -> {
                    courseCounts[1]++
                    courseCounts[2]++
                    courseCounts[3]++
                }
                Wahlmoeglichkeit.DRITTES_VIERTES -> {
                    courseCounts[2]++
                    courseCounts[3]++
                }
                Wahlmoeglichkeit.DURCHGEHEND -> {
                    courseCounts[0]++
                    courseCounts[1]++
                    courseCounts[2]++
                    courseCounts[3]++
                }
            }
        }

        return courseCounts
    }

    /**
     * Zählt die Wochenstunden für die beiden Schuljahre
     */
    fun weeklyCourses(): Pair<Int, Int> {
        // 3h p. GK + 5h p. Lk (um auf 10 für LKs zu kommen fehlen 4, Sport sind nur 2, dh. +3)
        val weekly = countCourses().map { it * 3 + 3 }
        return (weekly[0] + weekly[1]) / 2 to (weekly[2] + weekly[3]) / 2
    }

    /**
     * Alle 5 Prüfungsfächer
     */
    val pfs: List<Fach?>
        get() = listOf(lk1, lk2, pf3, pf4, pf5)

    /**
     * Prüfungsfächer 1 bis 4
     */
    @Suppress("PropertyName")
    val pf1_4: List<Fach?>
        get() = listOf(lk1, lk2, pf3, pf4)

    /**
     * Beide Leistungskurse
     */
    val lks: List<Fach?>
        get() = listOf(lk1, lk2)

    private var _kurse: Map<Fach, Wahlmoeglichkeit>? = null
    val kurse: Map<Fach, Wahlmoeglichkeit>
        get() = if (locked) _kurse!! else (gks + pfs.filterNotNull().associateWith { Wahlmoeglichkeit.DURCHGEHEND })

    private var locked = false

    /**
     * Verhindert eine Neuberechnung verschiedener Properties während das Objekt gelockt ist
     */
    fun lock() {
        _kurse = kurse
        locked = true
    }

    /**
     * Hebt den gelockten Zustand wieder auf
     */
    fun unlock() {
        _kurse = null
        locked = false
    }

    /**
     * Überprüft ob alle Felder gefüllt sind
     */
    val isComplete: Boolean
        get() = fremdsprachen.size >= 2 && (wpfs?.first != null) && gks.isNotEmpty() && lk1 != null && lk2 != null && pf3 != null && pf4 != null && pf5 != null

    /**
     * Entfernt LKs aus den PFs und GKs
     */
    fun updateLKs(lk1: Fach, lk2: Fach): KurswahlData =
        this.copy(lk1 = lk1, lk2 = lk2, gks = gks.filterKeys { it != lk1 && it != lk1 }).apply {
            if (pf3 == lk1 || pf3 == lk2) {
                this.pf3 = null
                this.wahlzeile = -1
            }
            if (pf4 == lk1 || pf4 == lk2) {
                this.pf4 = null
                this.wahlzeile = -1
            }
            if (pf5 == lk1 || pf5 == lk2) {
                this.pf5 = null
                this.wahlzeile = -1
            }
        }

    /**
     * Entfernt PFs aus den GKs
     */
    fun updatePFs(pf3: Fach, pf4: Fach, pf5: Fach, pf5_typ: Pf5Typ, wahlzeile: Int): KurswahlData =
        this.copy(
            pf3 = pf3,
            pf4 = pf4,
            pf5 = pf5,
            gks = gks.filterKeys { it != pf3 && it != pf4 && it != pf5 },
            pf5_typ = pf5_typ,
            wahlzeile = wahlzeile
        )

    /**
     * Entfernt Fremdsprachen und WPFs, die der Schüler nicht mehr hat aus den Kursen
     */
    fun updateWahlfaecher(fremdsprachenNew: List<Pair<Fach, Int>>, wpfsNew: Pair<Fach, Fach?>?): KurswahlData {
        val fachDif = (fremdsprachen - fremdsprachenNew.toSet()).map { it.first }.toMutableList()
        if (wpfs != null && wpfsNew != null) {
            if (wpfs!!.first != wpfsNew.first) fachDif += wpfs!!.first

            val second = wpfs!!.second
            if (second != null && second != wpfsNew.second) fachDif += second
        }
        return this.copy(fremdsprachen = fremdsprachenNew, wpfs = wpfsNew, gks = gks.filterKeys { it !in fachDif })
            .apply {
                if (lk1 in fachDif) {
                    this.lk1 = null
                    this.wahlzeile = -1
                }
                if (lk2 in fachDif) {
                    this.lk2 = null
                    this.wahlzeile = -1
                }
                if (pf3 in fachDif) {
                    this.pf3 = null
                    this.wahlzeile = -1
                }
                if (pf4 in fachDif) {
                    this.pf4 = null
                    this.wahlzeile = -1
                }
                if (pf5 in fachDif) {
                    this.pf5 = null
                }
            }

    }
}
