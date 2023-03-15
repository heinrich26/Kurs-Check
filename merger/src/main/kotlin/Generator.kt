/*
 * Copyright (c) 2022  Hendrik Horstmann
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

import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.SchoolConfig
import com.kurswahlApp.data.Wahlmoeglichkeit.*
import com.kurswahlApp.gui.wrapHtml
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
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
    var output by parser.argument(ArgType.String, "output", "Speicherort der generierten .csv-Datei")
        .optional()

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

        assert(chooser.selectedFile.isDirectory)

        input = chooser.selectedFile.absolutePath

        chooser.fileSelectionMode = JFileChooser.FILES_ONLY
        chooser.isMultiSelectionEnabled = false
        chooser.fileFilter = FileNameExtensionFilter("csv-Dateien", "csv")
        chooser.dialogTitle = "Ziel-Datei für die Tabelle auswählen"
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            output = chooser.selectedFile.absolutePath
        }

        try {
            run(schulId, input, output, object : PrintStream(System.out) {
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
        run(schulId, input, output, System.out) // CLI merger starten
    }

}

private fun run(schulId: String, directory: String, output: String?, out: PrintStream) {
    val fachData = SchoolConfig.getSchool(schulId)
        ?: throw RuntimeException("Die gegebene 'schulId' existiert nicht! Bitte versuchen sie es erneut!")


    val dirFile = File(directory)
    if (!dirFile.isDirectory) {
        throw RuntimeException("Der angegebene Pfad ist kein Ordner!")
    }


    val files = dirFile.listFiles { f -> f.extension == Consts.FILETYPE_EXTENSION }
        ?: throw RuntimeException("Ungültiger Pfad")

    if (files.isEmpty()) throw RuntimeException("Der Ordner war leer, keine Datei konnte erstellt werden!")

    // Dateien laden, ungültige aussortieren
    val wahlDataList = files.mapNotNull {
        try {
            fachData.loadKurswahl(it)
        } catch (e: Exception) {
            out.println("Fehler ${it.name}: Die Datei wurde für eine andere Schule erstellt oder ist ungültig, der/die Schüler*in muss seine/ihre Wahl wiederholen")
            null
        }
    }

    val outputFile = if (output != null) {
        File(output).let {
            if (it.isFile && it.extension.equals("csv", true)) it else {
                out.println("Ungültiger Pfad für die Output-Datei, nutze Default: $FILE_NAME im Ordner $dirFile!")
                File(dirFile, FILE_NAME)
            }
        }
    } else File(dirFile, FILE_NAME)

    val writer = outputFile.bufferedWriter(Charset.forName("UTF-8"))


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
                    ERSTES_ZWEITES -> listOf(GK, GK, null, null)
                    ERSTES_DRITTES -> listOf(GK, GK, GK, null)
                    ZWEITES_DRITTES -> listOf(null, GK, GK, null)
                    ZWEITES_VIERTES -> listOf(null, GK, GK, GK)
                    DRITTES_VIERTES -> listOf(null, null, GK, GK)
                    DURCHGEHEND -> listOf(GK, GK, GK, GK)
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
                pf5_typ.toString(), //pf5_typ
                *row // faecher p. Semester
            )
            filesProcessed++
        }
    }

    csvPrinter.close(true)

    out.println("$filesProcessed Dateien zusammengefügt")
}