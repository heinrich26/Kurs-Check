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

package com.kurswahlApp

import com.kurswahlApp.data.*
import com.kurswahlApp.gui.wrapHtml
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.Charset
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.concurrent.thread
import kotlin.system.exitProcess


const val LK = "lk"
const val PF3 = "pf3"
const val PF4 = "pf4"
const val PK5 = "pk5"
const val GK = "gk"

const val FILE_NAME = "kurswahlen.csv"

/**
 * Beschreibt das Ausgabeformat der erstellten Tabelle
 */
val CSV_HEADER = arrayOf(
    "vorname",
    "nachname",
    "geburtsdatum",
    "geburtsort",
    "staatsangehoerigkeit",
    "wahlzeile",
    "fs1",
    "fs1ab",
    "fs2",
    "fs2ab",
    "fs3",
    "fs3ab",
    "fs4",
    "fs4ab",
    "wpf1",
    "wpf2",
    "pk5_typ"
    // ...faecher
)

enum class TYP { CSV, PDF }

/**
 * Runnable, die KursWahl-Dateien in eine große CSV-Tabelle zusammenfasst.
 * @see CSV_HEADER
 */
fun main(args: Array<String>) {
    val parser = ArgParser(Consts.APP_NAME)

    val gui by parser.option(
        ArgType.Boolean, "gui", null,
        "Ob der Merger mit grafischer Oberfläche ausgeführt werden soll."
    ).default(false)

    var schulId by parser.argument(
        ArgType.String, "schulId",
        "ID der Schule für die Dateien zusammengefasst werden (Dateiname der Konfiguration für ihre Schule) z.B.: 'lili.json'",
    )

    var input by parser.argument(ArgType.String, "input", "Für die CSV-Generierung verwendeter Ordner")
        .optional().default(System.getProperty("user.dir"))
    var output by parser.argument(ArgType.String, "output", "Speicherort der generierten CSV-Datei bzw. PDF-Dateien")
        .optional()

    var action by parser.option(ArgType.Choice<TYP>(), "action", "a", "Ob PDFs oder eine CSV generiert werden sollen")
        .default(TYP.CSV)

    parser.parse(args)

    if (gui) {
        schulId = JOptionPane.showInputDialog(
            null,
            "Schul-ID mit <code>.json</code>-Endung:".wrapHtml(),
            "Schuld-ID eingeben",
            JOptionPane.QUESTION_MESSAGE
        ) ?: run {
            JOptionPane.showMessageDialog(null, "Keine Eingabe, Abbruch!", "Programmabbruch", JOptionPane.ERROR_MESSAGE)
            exitProcess(0)
        }

        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Ordner mit den Kurswahlen auswählen"
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(
                null,
                "Sie haben keinen Ordner ausgewählt!",
                "Vorgang abgebrochen",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        input = chooser.selectedFile.absolutePath

        chooser.fileSelectionMode = JFileChooser.FILES_ONLY
        chooser.isMultiSelectionEnabled = false
        chooser.fileFilter = FileNameExtensionFilter("csv-Dateien", "csv")
        chooser.dialogTitle = "Ziel-Datei für die Tabelle auswählen"
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            output = chooser.selectedFile.absolutePath
        }

        action = TYP.values().getOrElse(JOptionPane.showOptionDialog(null, "Aktion auswählen", "Auswählen", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, TYP.values(), TYP.CSV)
        ) { TYP.CSV }

        try {
            run(schulId, input, output, action, object : PrintStream(System.out) {
                override fun print(s: String?) {
                    super.print(s)
                    thread { JOptionPane.showMessageDialog(null, s, "Warnung", JOptionPane.WARNING_MESSAGE) }
                }
            })
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(null, e.localizedMessage, e.javaClass.name, JOptionPane.ERROR_MESSAGE)
            exitProcess(0)
        }
    } else {
        run(schulId, input, output, action, System.out) // CLI merger starten
    }

}

private fun run(schulId: String, directory: String, output: String?, action: TYP, out: PrintStream) {
    val fachData = SchoolConfig.getSchool(schulId)
        ?: throw RuntimeException("Die gegebene 'schulId' existiert nicht! Bitte versuchen sie es erneut!")

    val dirFile = File(directory)
    if (!dirFile.isDirectory) {
        throw RuntimeException("Der angegebene Pfad ist kein Ordner!")
    }

    val files = dirFile.listFiles { f -> f.extension.equals(Consts.FILETYPE_EXTENSION, true) }
        ?: throw RuntimeException("Ungültiger Pfad")

    if (files.isEmpty()) throw RuntimeException("Der Ordner war leer, keine Datei konnte erstellt werden!")

    val outputFile = output?.let { File(it) }

    if (action == TYP.CSV) {
        mergeToCSV(fachData, files, outputFile, out)
    } else {
        val pdfs = dirFile.listFiles { f -> f.extension.equals("pdf", true) } ?: throw RuntimeException("Ungültiger Pfad")
        convertToPDF(fachData, files, pdfs, outputFile?: File(dirFile, "output"), out)
    }
}


private fun convertToPDF(fachData: FachData, files: Array<out File>, pdfs: Array<out File>, outputDir: File, out: PrintStream) {
    try {
        outputDir.mkdirs()
    } catch (e: IOException) {
        out.println("Keine Berechtigung diesen Ordner zu erstellen")
        return
    }

    for (f in files) {
        val data: KurswahlData
        try {
            data = fachData.loadKurswahl(f)
        } catch (e: Exception) {
            out.println(e.stackTraceToString())
            out.println("Fehler ${f.name}: Die Datei wurde für eine andere Schule erstellt oder ist ungültig, der/die Schüler*in muss seine/ihre Wahl wiederholen")
            continue
        }

        val form: File
        try {
            form = pdfs.find {
                it.nameWithoutExtension.matches(
                    Regex(
                        fachData.fnamePattern!!
                            .replace("%vname%", data.vorname!!.replace(' ', '_'))
                            .replace("%nname%", data.nachname!!.replace(' ', '_'))
                    )
                )
            } ?: throw RuntimeException()
        } catch (ignored: RuntimeException) {
            out.println("Fehler ${f.name}: konnte keine PDF für ${data.vorname} ${data.nachname} finden.")
            continue
        }

        try {
            data.exportPDF(form, File(outputDir, form.name), fachData)
        } catch (ignored: Exception) {
            out.println("Fehler ${f.name}: Unerwarteter Fehler beim Export.")
        }
    }
}

private fun mergeToCSV(fachData: FachData, files: Array<out File>, outputFile: File?, out: PrintStream) {
    // Dateien laden, ungültige aussortieren
    val wahlDataList = files.mapNotNull {
        try {
            fachData.loadKurswahl(it)
        } catch (e: Exception) {
            out.println("Fehler ${it.name}: Die Datei wurde für eine andere Schule erstellt oder ist ungültig, der/die Schüler*in muss seine/ihre Wahl wiederholen")
            null
        }
    }

    val f = if (outputFile == null || !outputFile.isFile || !outputFile.extension.equals("csv", true)) {
        out.println("Ungültiger Pfad für die Output-Datei, nutze Default: $FILE_NAME im Ordner ${files[0].parentFile} !")
        File(files[0].parentFile, FILE_NAME)
    } else outputFile

    val writer = f.bufferedWriter(Charset.forName("UTF-8"))


    val filteredFaecher = fachData.faecher.filter { it.isKurs }
    val headerMixin =
        filteredFaecher.flatMap { listOf("${it.kuerzel}_1", "${it.kuerzel}_2", "${it.kuerzel}_3", "${it.kuerzel}_4") }
            .toTypedArray()

    val csvPrinter =
        CSVPrinter(writer, CSVFormat.Builder.create(CSVFormat.EXCEL).setHeader(*CSV_HEADER, *headerMixin).build())

    var filesProcessed = 0
    for (record in wahlDataList) {
        if (record.pfs.filterNotNull().size != 5) {
            out.println("Ungültige Kurswahl Datei, überspringe!")
            continue
        }

        with(record) {
            var skipped = 0
            val row = filteredFaecher.flatMap {
                when (gks[it]) {
                    com.kurswahlApp.data.Wahlmoeglichkeit.ERSTES_ZWEITES -> listOf(GK, GK, null, null)
                    com.kurswahlApp.data.Wahlmoeglichkeit.ERSTES_DRITTES -> listOf(GK, GK, GK, null)
                    com.kurswahlApp.data.Wahlmoeglichkeit.ZWEITES_DRITTES -> listOf(null, GK, GK, null)
                    com.kurswahlApp.data.Wahlmoeglichkeit.ZWEITES_VIERTES -> listOf(null, GK, GK, GK)
                    com.kurswahlApp.data.Wahlmoeglichkeit.DRITTES_VIERTES -> listOf(null, null, GK, GK)
                    com.kurswahlApp.data.Wahlmoeglichkeit.DURCHGEHEND -> listOf(GK, GK, GK, GK)
                    null -> when (pfs.indexOf(it)) {
                        0, 1 -> listOf(LK, LK, LK, LK)
                        2 -> listOf(PF3, PF3, PF3, PF3)
                        3 -> listOf(PF4, PF4, PF4, PF4)
                        4 -> listOf(PK5, PK5, PK5, PK5)
                        else -> {
                            skipped += 4
                            listOfNulls(4)
                        }
                    }
                }
            }.toTypedArray()

            // Überprüfen ob gks Kurse enthält, die wir nicht kennen
            if (row.size != gks.size * 4 + 20 + skipped /* 4*5 für die PFs */) {
                out.println("Ungültige Kurswahl Datei, überspringe!")
                return@with
            }

            val mappedSprachen = arrayOfNulls<Any?>(8)
            for (i in 0..3) {
                fremdsprachen.getOrNull(i)?.let {
                    mappedSprachen[2 * i] = it.first.kuerzel
                    mappedSprachen[2 * i + 1] = it.second
                } ?: break
            }

            csvPrinter.printRecord(
                vorname, // vorname
                nachname, // nachname
                geburtsdatum, // geburtsdatum
                geburtsort, // geburtsort
                staatsangehoerigkeit, // staatsangehoerigkeit
                wahlzeile, // wahlzeile
                *mappedSprachen, // fs1 - fs4
                wpfs!!.first.kuerzel, // wpf1
                wpfs!!.second?.kuerzel, // wpf2
                pf5Typ.toString(), //pf5_typ
                *row // faecher p. Semester
            )
            filesProcessed++
        }
    }

    csvPrinter.close(true)

    out.println("$filesProcessed Dateien zusammengefügt")
}