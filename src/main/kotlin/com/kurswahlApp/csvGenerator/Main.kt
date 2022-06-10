package com.kurswahlApp.csvGenerator

import com.kurswahlApp.data.Wahlmoeglichkeit.*
import com.kurswahlApp.gui.Consts
import com.kurswahlApp.listOfNulls
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import com.kurswahlApp.readDataStruct
import java.io.File


const val LK = "lk"
const val PF3 = "pf3"
const val PF4 = "pf4"
const val PF5 = "pf5"
const val GK = "gk"

const val FILE_NAME = "kurswahlen.csv"

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
    "pf5_typ"
    // ...faecher
)

fun main(args: Array<String>) {
    val fachData = readDataStruct()


    val dirFile =
        if (args.isEmpty()) {
            File(System.getProperty("user.dir"))
        } else if (args.size == 1) {
            File(args[0]).let {
                if (it.isDirectory) it
                else throw RuntimeException("Der angegebene Pfad ist kein Ordner!")
            }
        } else throw RuntimeException("Zu viele Argumente! --csvGen benötigt maximal einen Parameter!")


    val files = dirFile.listFiles { file -> file.extension == Consts.FILETYPE_EXTENSION }
        ?: throw RuntimeException("Ungültiger Pfad")
    if (files.isEmpty()) throw RuntimeException("Der Ordner war leer, keine Datei konnte erstellt werden!")

    val wahlDataList = files.map { fachData.loadKurswahl(it) }

    val writer = File(dirFile, FILE_NAME).bufferedWriter()


    val headerMixin =
        fachData.faecher.flatMap { listOf("${it.kuerzel}_1", "${it.kuerzel}_2", "${it.kuerzel}_3", "${it.kuerzel}_4") }
            .toTypedArray()

    val csvPrinter =
        CSVPrinter(writer, CSVFormat.Builder.create(CSVFormat.EXCEL).setHeader(*CSV_HEADER, *headerMixin).build())

    for (record in wahlDataList) {
        if (record.pfs.filterNotNull().size != 5) {
            println("Ungültige Kurswahl Datei, überspringe!")
            continue
        }

        with(record) {
            var skipped = 0
            val row = fachData.faecher.flatMap {
                when (gks[it]) {
                    ERSTES_ZWEITES -> listOf(GK, GK, null, null)
                    ERSTES_DRITTES -> listOf(GK, GK, GK, null)
                    ZWEITES_VIERTES -> listOf(null, GK, GK, GK)
                    DRITTES_VIERTES -> listOf(null, null, GK, GK)
                    DURCHGEHEND -> listOf(GK, GK, GK, GK)
                    null -> when (pfs.indexOf(it)) {
                        0, 1 -> listOf(LK, LK, LK, LK)
                        2 -> listOf(PF3, PF3, PF3, PF3)
                        3 -> listOf(PF4, PF4, PF4, PF4)
                        4 -> listOf(PF5, PF5, PF5, PF5)
                        else -> {
                            skipped += 4
                            listOfNulls(4)
                        }
                    }
                }
            }.toTypedArray()

            // Überprüfen ob gks Kurse enthält, die wir nicht kennen
            if (row.size != gks.size * 4 + 20 + skipped /* 4*5 für die PFs */) {
                println("Ungültige Kurswahl Datei, überspringe!")
                return@with
            }

            val mappedSprachen = arrayOfNulls<Any?>(8)
            for (i in 0..3) {
                fremdsprachen.getOrNull(i)?.let {
                    mappedSprachen[2 * i] = it.first
                    mappedSprachen[2 * i + 1] = it.second
                } ?: break
            }

            csvPrinter.printRecord(
                "Hendrik Sven", // vorname
                "Horstmann", // nachname
                "27.09.2005", // geburtsdatum
                "Berlin", // geburtsort
                "Deutsch", // staatsangehoerigkeit
                wahlzeile, // wahlzeile
                *mappedSprachen, // fs1 - fs4
                wpfs!!.first.kuerzel, // wpf1
                wpfs!!.second?.kuerzel, // wpf2
                pf5_typ.toString(), //pf5_typ
                *row // faecher p. Semester
            )
        }
    }

    csvPrinter.close(true)
}