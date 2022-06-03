import data.Fach
import data.KurswahlData
import data.Wahlmoeglichkeit

val eng = Fach("Englisch", "En", 1, true)
val ma = Fach("Mathe", "Ma", 2, true)
val de = Fach("Deutsch", "De", 1, true)
val inf = Fach("Informatik", "Inf", 2, true)
val geo = Fach("Geografie", "Geo", 3, true)
val sp = Fach("Sport", "Sp", 0, false)
val ge = Fach("Geschichte", "Ge", 3, true)
val spa = Fach("Spanisch", "Spa", 1, true)
val lat = Fach("Latein", "Lat", 1, false)
val fr = Fach("Französisch", "Fr", 1, true)

val fremdsprachen = listOf(eng, spa, lat, fr)
var faecher = listOf(eng, ma, de, inf, geo, sp, ge, spa, lat, fr)
val wpfs = listOf(inf, lat)

val testFachdata = readDataStruct()

val testKurswahl = KurswahlData().apply {
    lk1 = testFachdata.faecherMap["En"]!!
    lk2 = testFachdata.faecherMap["Spa"]!!
    gks = testFachdata.faecher.associateWith { Wahlmoeglichkeit.DRITTES_VIERTES }
    fremdsprachen = listOf(testFachdata.faecherMap["En"]!! to 3, testFachdata.faecherMap["Spa"]!! to 7)
    wpfs = inf to null
}