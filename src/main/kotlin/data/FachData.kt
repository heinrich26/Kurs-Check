package data

class FachData(
    val feacher: List<Fach>,
    val fremdsprachen: List<Fach>,
    val wpfs: List<Fach>,
    val pflichtfaecher: List<Pair<Fach, Wahlmoeglichkeit>>,
    val wahlzeilen: Map<Int, Wahlzeile>
) {
    /**
     * Gibt die die LKs zurück
     */
    val lks: List<Fach>
        get() = feacher.filter { it.lk }

    /**
     * Gibt alle möglichen Wahlzeilen für die gegebenen LKs zurück
     */
    fun possibleWahlzeilen(lk1: Fach, lk2: Fach): Map<Int, Wahlzeile> {
        return mapOf()
    }
}