package data

class FachData(
    val feacher: List<Fach>,
    val fremdsprachen: List<Fach>,
    val wpfs: List<Fach>,
    val pflichtfaecher: List<Pair<Fach, Wahlmoeglichkeit>>
) {
    /**
     * Gibt die die LKs zurück
     */
    val lks: List<Fach>
        get() = feacher.filter { it.lk }
}