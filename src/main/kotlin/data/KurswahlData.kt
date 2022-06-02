package data

import com.fasterxml.jackson.databind.annotation.JsonSerialize


data class KurswahlData(
    var lk1: Fach? = null,
    var lk2: Fach? = null,
    var pf3: Fach? = null,
    var pf4: Fach? = null,
    var pf5: Fach? = null,
    var pf5_typ: Pf5Typ = Pf5Typ.PRAESENTATION,
    @JsonSerialize(using = MapSerializer::class) var gks: Map<Fach, Wahlmoeglichkeit> = emptyMap(),
    var fremdsprachen: List<Pair<Fach, Int>> = emptyList(),
    var wpfs: Pair<Fach, Fach?>? = null
) {

    /**
     * Zählt die gewählten Kurse pro Semester
     */
    fun countCourses(): Array<Int> {
        val courseCounts = arrayOf(4, 4, 4, 4)
        for ((_, moegl) in gks) {
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

    val pfs: List<Fach?>
        get() = listOf(lk1, lk2, pf3, pf4, pf5)

    val pf1_4: List<Fach?>
        get() = listOf(lk1, lk2, pf3, pf4)

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
     * Entfernt LKs aus den PFs und GKs
     */
    fun updateLKs(lk1: Fach, lk2: Fach): KurswahlData =
        this.copy(lk1 = lk1, lk2 = lk2, gks = gks.filterKeys { it != lk1 && it != lk1 }).apply { 
            if (pf3 == lk1 || pf3 != lk2) this.pf3 = null
            if (pf4 == lk1 || pf4 != lk2) this.pf4 = null
            if (pf5 == lk1 || pf5 != lk2) this.pf5 = null
        }

    /**
     * Entfernt PFs aus den GKs
     */
    fun updatePFs(pf3: Fach, pf4: Fach, pf5: Fach): KurswahlData =
        this.copy(pf3 = pf3, pf4 = pf4, pf5 = pf5, gks = gks.filterKeys { it != pf3 && it != pf4 && it != pf5 })

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
                if (lk1 in fachDif)
                    this.lk1 = null
                if (lk2 in fachDif)
                    this.lk1 = null
                if (pf3 in fachDif)
                    this.pf3 = null
                if (pf4 in fachDif)
                    this.pf4 = null
                if (pf5 in fachDif)
                    this.pf5 = null
            }

    }
}
