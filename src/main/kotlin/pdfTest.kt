import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter


fun main() {
    val reader = PdfReader(getResourceURL("Uebersichtsplan.pdf")!!.file)
    reader.setUnethicalReading(true) // irgend ein Horst hat sich gedacht: lass passwortgeschützt machen, dumbatz!
    val writer = PdfWriter("./src/main/resources/out.pdf")
    val document = PdfDocument(reader, writer)
    val stamper = PdfAcroForm.getAcroForm(document, false)
    val fields = stamper.formFields
    val keys = fields.keys

    println(keys.joinToString("', '", "'", "'"))

    stamper.replaceField(
        "Name, Vornamen (alle!)",
        fields["Name, Vornamen (alle!)"]!!.setValue("Kurek, Togert Tiburtius")
    )

    document.close()
}