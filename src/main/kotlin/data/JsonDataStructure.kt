package data


class JsonDataStructure(
    private val faecher: Map<String, Fach>,
    private val pflichtfaecher: Map<String, Wahlmoeglichkeit>,
    private val fremdsprachen: List<String>,
    private val wpfs: List<String>,
    private val regeln: List<RegelHolder>,
    private val wahlzeilen: Map<Int, Wahlzeile>,
    private val wildcards: Map<String, List<String>>,
    private val wzWildcards: List<String>,
    private val minKurse: Int,
    private val maxKurse: Int
) {
    /**
     * Das [FachData] Objekt erstellen,
     * indem wenn nötig eingelesene Daten umgeformt werden
     */
    fun toFachData(): FachData {
        val mappedWildcards = wildcards.map { (key, value) -> key to value.map { faecher[it]!! } }.toMap()

        return FachData(
            faecher = faecher.values.toList(),
            fremdsprachen = fremdsprachen.map { faecher[it]!! },
            pflichtfaecher = pflichtfaecher.map { (k, v) -> faecher[k]!! to v }.toMap(),
            wpfs = wpfs.map { faecher[it]!! },
            regeln = regeln.map { it.regel },
            wahlzeilen = wahlzeilen,
            wildcards = mappedWildcards,
            wzWildcards = wzWildcards.associateWith { mappedWildcards[it]!! },
            minKurse = minKurse,
            maxKurse = maxKurse
        )
    }
}