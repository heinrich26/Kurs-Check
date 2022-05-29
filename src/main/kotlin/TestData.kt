import data.*

val eng = Fach("Englisch", "E", 1, true)
val ma = Fach("Mathe", "Ma", 2, true)
val de = Fach("Deutsch", "De", 1, true)
val inf = Fach("Informatik", "Inf", 2, true)
val geo = Fach("Geographie", "Geo", 3, true)
val sp = Fach("Sport", "Sp", 0, false)
val ge = Fach("Geschichte", "Ge", 3, true)
val spa = Fach("Spanisch", "Spa", 1, true)
val lat = Fach("Latein", "Lat", 1, false)
val fr = Fach("Französisch", "Fr", 1, true)

val fremdsprachen = listOf(eng, spa, lat, fr)
var faecher = listOf(eng, ma, de, inf, geo, sp, ge, spa, lat, fr)
val wpfs = listOf(inf, lat)


val testKurswahl = KurswahlData().apply {
    lk1 = faecher[0]
    lk2 = faecher[1]
    pf3 = faecher[2]
    pf4 = faecher[3]
    pf5 = faecher[4]
    gks = faecher.associateWith { Wahlmoeglichkeit.DRITTES_VIERTES }
    fremdsprachen = listOf(eng to 3, spa to 7)
    wpfs = inf to null
}

//val testFachdata = FachData(faecher, fremdsprachen, wpfs, mapOf(ma to Wahlmoeglichkeit.DURCHGEHEND, de to Wahlmoeglichkeit.DURCHGEHEND, sp to Wahlmoeglichkeit.DURCHGEHEND), )
val testFachdata = readDataStruct()