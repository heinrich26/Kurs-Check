package data

class JsonDataStructure {
    var availableCourses: Map<String, Fach>? = null

    var aufgabenfelder: Map<Int, List<String>>? = null

    override fun toString(): String {
        return "Kurse: $availableCourses\nAufgabenfelder: $aufgabenfelder\nWahlzeilen: $wahlzeile"
    }

    var wahlzeile: Map<Int, Wahlzeile>? = null
}