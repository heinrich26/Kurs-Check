package data

class JsonDataStructure(
    val faecher: Map<String, Fach>,
    val pflichtfaecher: Map<String, Wahlmoeglichkeit>,
    val wahlzeilen: Map<Int, Wahlzeile>,
    val fremdsprachen: List<String>,
    val wpfs: List<String>,
    val wildcards: Map<String, List<String>>
) {

    override fun toString(): String {
        return "Kurse: $faecher\nWahlzeilen: $wahlzeilen"
    }

    /**
     * Das [FachData] Objekt erstellen,
     * indem wenn nötig eingelesene Daten umgeformt werden
     */
    fun toFachData(): FachData = FachData(
        faecher = faecher.values.toList(),
        fremdsprachen = fremdsprachen.map { faecher[it]!! },
        pflichtfaecher = pflichtfaecher.map { (k, v) -> faecher[k]!! to v }.toMap(),
        wahlzeilen = wahlzeilen,
        wpfs = wpfs.map { faecher[it]!! },
        wildcards = wildcards.map { (key, value) -> key to value.map { faecher[it]!! } }.toMap()
    )
}