/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kurswahlApp.data.lusd_pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
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

fun main(args: Array<String>) {
    val fname = args.getOrElse(0) { "res/formular.pdf" }
    println(fname)
    val doc = PDDocument.load(File(fname))
    val catalog = doc.documentCatalog
    val acroForm = catalog.acroForm

    
    val pdfStripper = PDFTextStripper()
    pdfStripper.startPage = 1
    pdfStripper.endPage = 1
    val parsedText = pdfStripper.getText(doc)
    println(parsedText)

    val felder = acroForm.fields.mapNotNull {
        if (it.fullyQualifiedName == "RadioGroupAcroFormField_Pruefungskomponente") null
        else try {
            Feld(it.fullyQualifiedName.split('$'), it.valueAsString)
        } catch (e: Exception) {
            return@mapNotNull null
        }
    }
    felder.groupBy(Feld::id).mapKeys { (k, v) ->  "${v[0].name} ($k)" }.mapValues { (_, v) -> v.map(Feld::value) }.forEach(::println)

}