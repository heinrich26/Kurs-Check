package data

class FachData(
    val feacher: List<Fach>,
    val fremdsprachen: List<Fach>,
    val wpfs: List<Fach>,
) {
    /**
     * Gibt die die LKs zurück
     */
    val lks: List<Fach>
        get() = feacher.filter { it.lk }
}