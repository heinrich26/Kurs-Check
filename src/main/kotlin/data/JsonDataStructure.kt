package data

class JsonDataStructure(
    val faecher: Map<String, Fach>,
    val aufgabenfelder: Map<Int, List<String>>,
    val wahlzeilen: Map<Int, Wahlzeile>,
    val fremdsprachen: List<String>,
    val wpfs: List<String>,
    val naturwissenschaften: List<String>,
    val wildcards: Map<String, List<String>>
) {

    override fun toString(): String {
        return "Kurse: $faecher\nAufgabenfelder: $aufgabenfelder\nWahlzeilen: $wahlzeilen"
    }

    fun toFachData(): FachData = FachData(
        faecher = faecher.values.toList(),
        fremdsprachen = fremdsprachen.map { faecher[it]!! },
        pflichtfaecher = emptyList(), // TODO Pflichtfächer reinschreiben!
        wahlzeilen = wahlzeilen,
        wpfs = wpfs.map { faecher[it]!! },
        naturwissenschaften = naturwissenschaften.map { faecher[it]!! },
        wildcards = wildcards.map { (key, value) -> key to value.map { faecher[it]!! } }.toMap()
    )
}