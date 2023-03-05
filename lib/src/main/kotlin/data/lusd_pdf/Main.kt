package com.kurswahlApp.data.lusd_pdf

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File

/**
 * @property fach Generelles Fach (Geschichte und Geschichte Bili)
 * @property id Id des Fachs
 */
data class Feld(val name: String, val af: Int, val fach: Int, val id: Int, val typ: String, val value: String) {
    constructor(args: List<String>, value: String) : this(args[0], args[1].toInt(), args[2].toInt(), args[3].toInt(), args[4].dropLast(2), value)
}

const val SCHUELER_ID_FIELD = "SchuelerId"
const val JAHRGANG_ID_FIELD = "AbiturJahrgangId"

const val PF_5_TYP_FIELD = "RadioGroupAcroFormField_Pruefungskomponente"

fun main() {
    val fname = "E:\\Users\\Hendrik\\Documents\\Schule\\Sek 2\\Informatik\\s_s.pdf"
    val doc = PDDocument.load(File(fname))
    val catalog = doc.documentCatalog
    val acroForm = catalog.acroForm

    val felder = acroForm.fields.mapNotNull {
        if (it.fullyQualifiedName == "RadioGroupAcroFormField_Pruefungskomponente") null
        else try {
            Feld(it.fullyQualifiedName.split('$'), it.valueAsString)
        } catch (e: Exception) {
            return@mapNotNull null
        }
    }
    felder.groupBy { it.id }.mapKeys { (k, v) ->  "${v[0].name} ($k)" }.mapValues { (_, v) -> v.map { it.value } }.forEach { println(it) }

}