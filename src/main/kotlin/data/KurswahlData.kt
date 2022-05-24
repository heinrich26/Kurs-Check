package data

data class KurswahlData(
    var lk1: Fach? = null,
    var lk2: Fach? = null,
    var pf3: Fach? = null,
    var pf4: Fach? = null,
    var pf5: Fach? = null,
    var pk3: Fach? = null,
    var pk4: Fach? = null,
    var pf5_typ: Pf5Typ = Pf5Typ.PRAESENTATION,
    var gks: List<Pair<Fach, Wahlmoeglichkeit>> = emptyList(),
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
    fun weeklyCourses() : Pair<Int, Int> {
        val weekly = countCourses().map{ it * 3 + 3 } // 3h p. GK + 5h p. Lk (um auf 10 für lks zu kommen fehlen 4, sport sind nur 2, dh +3)
        return (weekly[0] + weekly[1]) / 2 to (weekly[2] + weekly[3]) / 2
    }

    val pfs: List<Fach?>
        get() = listOf(lk1, lk2, pf3, pf4, pf5, pk3 , pk4)
}
