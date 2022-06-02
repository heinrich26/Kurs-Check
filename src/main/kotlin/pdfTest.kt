import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import data.Fach
import data.Wahlmoeglichkeit


fun main() {
    val reader = PdfReader(getResourceURL("Uebersichtsplan.pdf")!!.file)
    reader.setUnethicalReading(true) // irgend ein Horst hat sich gedacht: lass passwortgeschützt machen, dumbatz!
    val writer = PdfWriter("./src/main/resources/out.pdf")
    val document = PdfDocument(reader, writer)
    val stamper = PdfAcroForm.getAcroForm(document, false)
    val fields = stamper.formFields
    val keys = fields.keys

    println(keys.joinToString("', '", "'", "'")) // gibt namen der Felder aus

    stamper.replaceField(
        "Name, Vornamen (alle!)",
        fields["Name, Vornamen (alle!)"]!!.setValue("Kurek, Togert Tiburtius")
    )

    val data = testKurswahl

//    val mapping = mapOf("Ma" to listOf("PF Ma", "Q1 Ma", "Q2 Ma", "Q3 Ma", "Q4 Ma"))
    val mapping = data.kurse.keys.associateWith { kurs -> arrayOf("PF ${kurs.kuerzel}", "Q1 ${kurs.kuerzel}", "Q2 ${kurs.kuerzel}") }

    val startIndicies = mapOf(1 to 0, 2 to 8, 3 to 13)

    val agfelder = mutableMapOf<Int, MutableList<Pair<Fach, Wahlmoeglichkeit>>>()
    for ((fach, wmoegl) in data.kurse.entries) {
        val feld = fach.aufgabenfeld
        if (!agfelder.containsKey(feld))
            agfelder[feld] = mutableListOf(fach to wmoegl)
        else
            agfelder[feld]!!.add(fach to wmoegl)
    }

        data.gks.onEachIndexed { i, (fach, wmoegl) ->
            stamper.replaceField("$i $0", fields["$i $0"]!!.setValue(fach.name))
            // TODO kreuze machen
        }
//    for ((fach, wahlmoegl) in ) {
//        when (wahlmoegl) {
//            data.Wahlmoeglichkeit.ERSTES_ZWEITES -> // 1, 2
//            data.Wahlmoeglichkeit.ERSTES_DRITTES -> // 1, 2, 3
//            data.Wahlmoeglichkeit.ZWEITES_VIERTES -> // ..
//            data.Wahlmoeglichkeit.DRITTES_VIERTES -> TODO()
//            data.Wahlmoeglichkeit.DURCHGEHEND -> TODO()
//        }
//    }


    document.close()
}