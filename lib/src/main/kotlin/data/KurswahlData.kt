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

package com.kurswahlApp.data

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonAppend
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.kurswahlApp.data.Consts.JAHRGANG_ID_FIELD
import com.kurswahlApp.data.Consts.PF_5_TYP_FIELD
import com.kurswahlApp.data.Consts.SCHUELER_ID_FIELD
import com.kurswahlApp.data.Wahlmoeglichkeit.*
import com.kurswahlApp.data.lusd_pdf.FeldZeile
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import java.io.File
import java.io.IOException
import java.time.LocalDate

/**
 * Beinhaltet die Daten der Kurswahl
 *
 * Sollte niemals vor [FachData] ohne [readJsonVersion] erstellt werden.
 *
 * @property readJsonVersion Version der bei der Erstellung verwendeten [FachData], um einen Mix von Formaten vorzubeugen.
 */
@Suppress("PropertyName", "PrivatePropertyName")
@JsonAppend(attrs = [JsonAppend.Attr(value = "jsonVersion")], prepend = true)
@JsonIncludeProperties(
    "jsonVersion", "lk1", "lk2", "pf3", "pf4", "pf5", "pf5_typ", "gks", "fremdsprachen", "wpfs", "klasse",
    "wahlzeile", "vorname", "nachname", "geburtsdatum", "geburtsort", "staatsangehoerigkeit", "schulId"
)
data class KurswahlData(
    var lk1: Fach? = null,
    var lk2: Fach? = null,
    var pf3: Fach? = null,
    var pf4: Fach? = null,
    var pf5: Fach? = null,
    var pf5_typ: Pf5Typ = Pf5Typ.PRAESENTATION,
    var gks: Map<Fach, Wahlmoeglichkeit> = emptyMap(),
    @get:JsonSerialize(using = ListOfPairSerializer::class) var fremdsprachen: List<Pair<Fach, Int>> = emptyList(),
    var wpfs: Pair<Fach, Fach?>? = null,
    var wahlzeile: Int = -1,
    val klasse: String? = null,
    val pflichtfaecher: Map<Fach, Wahlmoeglichkeit>,

    // Persönliche Daten
    var vorname: String? = null,
    var nachname: String? = null,
    @get:JsonSerialize(using = LocalDateSerializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    var geburtsdatum: LocalDate? = null,
    var geburtsort: String? = null,
    var staatsangehoerigkeit: String = "DE",

    val readJsonVersion: Pair<Int, Int>,
    val schulId: String
) {

    companion object {
        @JvmStatic
        @JsonCreator
        @Throws(IllegalArgumentException::class)
        fun fromJson(
            @JsonProperty @JsonDeserialize(using = VersionDeserializer::class) jsonVersion: Pair<Int, Int>,
            @JsonProperty lk1: String,
            @JsonProperty lk2: String,
            @JsonProperty pf3: String,
            @JsonProperty pf4: String,
            @JsonProperty pf5: String,
            @JsonProperty pf5_typ: Pf5Typ,
            @JsonProperty gks: Map<String, Wahlmoeglichkeit>,
            @JsonProperty fremdsprachen: Map<String, Int>,
            @JsonProperty wpfs: Pair<String, String?>,
            @JsonProperty klasse: String?,
            @JsonProperty wahlzeile: Int,
            @JsonProperty vorname: String,
            @JsonProperty nachname: String,
            @JsonProperty
            @JsonDeserialize(using = LocalDateDeserializer::class)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            geburtsdatum: LocalDate,
            @JsonProperty geburtsort: String,
            @JsonProperty staatsangehoerigkeit: String,
            @JsonProperty schulId: String,
            @JacksonInject fachDataMirror: FachDataMirror
        ): KurswahlData {
            val fachData: FachData = fachDataMirror.get(schulId)
                ?: throw IllegalArgumentException("Die Schule der Nutzerdaten passt nicht zu den Fach-Daten!")


            return KurswahlData(
                lk1 = fachData.faecherMap[lk1].let { if (it != null && it.isLk) it else null },
                lk2 = fachData.faecherMap[lk2].let { if (it != null && it.isLk) it else null },
                pf3 = fachData.faecherMap[pf3],
                pf4 = fachData.faecherMap[pf4],
                pf5 = fachData.faecherMap[pf5],
                pf5_typ = pf5_typ,
                gks = gks.mapKeys { fachData.faecherMap[it.key]!! },
                fremdsprachen = fremdsprachen.map { fachData.faecherMap[it.key]!! to it.value },
                wpfs = if (wpfs.first !in fachData.faecherMap.keys) null else fachData.faecherMap[wpfs.first]!! to fachData.faecherMap[wpfs.second],
                wahlzeile = wahlzeile,
                klasse = klasse,
                pflichtfaecher = fachData.pflichtfaecher,

                vorname = vorname,
                nachname = nachname,
                geburtsdatum = geburtsdatum,
                geburtsort = geburtsort,
                staatsangehoerigkeit = staatsangehoerigkeit,

                readJsonVersion = jsonVersion,
                schulId = schulId
            )
        }

        /**
         * Läd eine Kurswahl aus einer LUSD PDF
         * Achtung: Wahlpflichtfächer, Fremdsprachen und die Klasse gehen dabei verloren!
         */
        /*@JvmStatic
        fun fromPDF(file: File, fachData: FachData, kurswahlData: KurswahlData? = null): KurswahlData {
            println("ThreadID: ${Thread.currentThread().id}")

            // Fächer umkehren, da associateBy das letzte Vorkommen nutzt, wir aber das 1. brauchen
            val fachIdMap = fachData.faecher.reversed().associateBy { it.lusdId }

            lateinit var pk5TypFeld: PDField

            val doc = PDDocument.load(file)

            val felder = fachIdMap.values.associateWith { FeldZeile() }
            doc.documentCatalog.acroForm.fields.forEach {
                when (val name = it.fullyQualifiedName) {
                    SCHUELER_ID_FIELD, JAHRGANG_ID_FIELD -> {}

                    PF_5_TYP_FIELD -> {
                        pk5TypFeld = it
                    }

                    else -> {
                        val parts = name.split('$')
                        val id = parts[3].toInt()
                        val zeile = felder[fachIdMap[id]]!!
                        when (parts.last()) {
                            "LK1_0" -> zeile.lk1 = it
                            "LK2_0" -> zeile.lk2 = it
                            "LK3_0" -> zeile.lk3 = it
                            "PF3_0" -> zeile.pf3 = it
                            "PF4_0" -> zeile.pf4 = it
                            "PK5_0" -> zeile.pk5 = it
                            "Q1_0" -> zeile.q1 = it
                            "Q2_0" -> zeile.q2 = it
                            "Q3_0" -> zeile.q3 = it
                            "Q4_0" -> zeile.q4 = it
                        }
                    }
                }
            }

            val ref = PDFReference(doc, felder, pk5TypFeld)


            return kurswahlData.copy(lusdPdfReference = ref)
        }*/
    }

    /**
     * Zählt die gewählten Kurse pro Semester
     */
    fun countCourses(excludeExtras: Boolean = false): IntArray {
        val courseCounts = intArrayOf(0, 0, 0, 0)
        for ((fach, moegl) in kurse) {
            // Fächer überspringen die nicht zur maximalen Kurszahl/Semester zählen, da sie auf anderen Schienen liegen!
            if (fach.isExtra && excludeExtras) continue

            when (moegl) {
                ERSTES_ZWEITES -> {
                    courseCounts[0]++
                    courseCounts[1]++
                }

                ERSTES_DRITTES -> {
                    courseCounts[0]++
                    courseCounts[1]++
                    courseCounts[2]++
                }

                ZWEITES_DRITTES -> {
                    courseCounts[1]++
                    courseCounts[2]++
                }

                ZWEITES_VIERTES -> {
                    courseCounts[1]++
                    courseCounts[2]++
                    courseCounts[3]++
                }

                DRITTES_VIERTES -> {
                    courseCounts[2]++
                    courseCounts[3]++
                }

                DURCHGEHEND -> {
                    courseCounts[0]++
                    courseCounts[1]++
                    courseCounts[2]++
                    courseCounts[3]++
                }
            }
        }

        return courseCounts
    }

    /**
     * Zählt die Wochenstunden für die beiden Schuljahre
     */
    fun weeklyCourses(): Pair<Int, Int> {
        // 3h p. GK + 5h p. Lk (um auf 10 für LKs zu kommen fehlen 4, Sport sind nur 2, dh. +3)
        val weekly = countCourses().map { it * 3 + 3 }
        return (weekly[0] + weekly[1]) / 2 to (weekly[2] + weekly[3]) / 2
    }

    private var _pfs: List<Fach?>? = null

    /**
     * Alle 5 Prüfungsfächer
     */
    val pfs: List<Fach?>
        get() = if (locked) _pfs!! else listOf(lk1, lk2, pf3, pf4, pf5)


    private var _pf1_4: List<Fach?>? = null

    /**
     * Prüfungsfächer 1 bis 4
     */
    val pf1_4: List<Fach?>
        get() = if (locked) _pf1_4!! else listOf(lk1, lk2, pf3, pf4)

    /**
     * Beide Leistungskurse
     */
    val lks: List<Fach?>
        get() = listOf(lk1, lk2)

    private var _kurse: Map<Fach, Wahlmoeglichkeit>? = null

    /**
     * Alle gewählten Kurse, einschließlich Prüfungsfächern
     */
    val kurse: Map<Fach, Wahlmoeglichkeit>
        get() = if (locked) _kurse!! else (gks + pfs.filterNotNull().associateWith { DURCHGEHEND })

    private var locked = false

    /**
     * Verhindert eine Neuberechnung verschiedener Properties während das Objekt gelockt ist
     */
    fun lock() {
        _kurse = kurse
        _pfs = pfs
        _pf1_4 = pf1_4
        locked = true
    }

    /**
     * Hebt den gelockten Zustand wieder auf
     */
    fun unlock() {
        _kurse = null
        _pfs = null
        _pf1_4 = null
        locked = false
    }

    /**
     * Überprüft ob die Wahl exportiert werden kann und wenn nicht,
     * wird eine Nachricht mit noch zu erledigenden Aktionen zurückgegeben
     */
    fun check(): String? {
        var errorMsg = ""

        if (fremdsprachen.isEmpty())
            errorMsg = "Deine 2 oder mehr Fremdsprachen angeben!\n"
        if (wpfs?.first == null)
            errorMsg += "Dein Wahlpflichtfach angeben!\n"
        if (lk1 == null || lk2 == null)
            errorMsg += "Leistungskurse wählen!\n"
        if (pf3 == null || pf4 == null || pf5 == null)
            errorMsg += "Deine weiteren Prüfungsfächer wählen!\n"
        if (vorname == null) /* wenn eins null ist, sind alle null || nachname == null || geburtsdatum == null || geburtsort == null*/
            errorMsg += "Deine persönlichen Daten eintragen!"

        return "Bevor du deine Kurswahl exportieren kannst, musst du noch folgendes erledigen:\n" + errorMsg.ifEmpty { return null }
    }

    /**
     * Entfernt LKs aus den PFs und GKs
     */
    fun updateLKs(lk1: Fach, lk2: Fach): KurswahlData =
        this.copy(lk1 = lk1, lk2 = lk2, gks = gks.filterKeys { it != lk1 && it != lk1 }).apply {
            if (pf3 == lk1 || pf3 == lk2) {
                this.pf3 = null
                this.wahlzeile = -1
            }
            if (pf4 == lk1 || pf4 == lk2) {
                this.pf4 = null
                this.wahlzeile = -1
            }
            if (pf5 == lk1 || pf5 == lk2) {
                this.pf5 = null
                this.wahlzeile = -1
            }

            updatePflichtfaecher()
        }

    /**
     * Entfernt PFs aus den GKs
     */
    fun updatePFs(pf3: Fach, pf4: Fach, pf5: Fach, pf5_typ: Pf5Typ, wahlzeile: Int): KurswahlData =
        this.copy(
            pf3 = pf3,
            pf4 = pf4,
            pf5 = pf5,
            gks = gks.filterKeys { it != pf3 && it != pf4 && it != pf5 },
            pf5_typ = pf5_typ,
            wahlzeile = wahlzeile
        ).apply {
            updatePflichtfaecher()
        }

    /**
     * Entfernt Fremdsprachen und WPFs, die der Schüler nicht mehr hat aus den Kursen
     */
    fun updateWahlfaecher(
        fremdsprachenNew: List<Pair<Fach, Int>>,
        wpfsNew: Pair<Fach, Fach?>?,
        klasse: String?
    ): KurswahlData {
        fun checkKlasse(fach: Fach?) = fach?.nurFuer?.contains(klasse) != false

        val fachDif = (fremdsprachen - fremdsprachenNew.toSet()).map { it.first }.toMutableList()
        if (wpfs != null && wpfsNew != null) {
            if (wpfs!!.first != wpfsNew.first) fachDif += wpfs!!.first

            val second = wpfs!!.second
            if (second != null && second != wpfsNew.second) fachDif += second
        }

        return this.copy(
            fremdsprachen = fremdsprachenNew,
            wpfs = wpfsNew,
            klasse = klasse,
            gks = gks.filterKeys { it !in fachDif && checkKlasse(it) })
            .apply {
                if (lk1 in fachDif || !checkKlasse(lk1)) {
                    this.lk1 = null
                    this.wahlzeile = -1
                }
                if (lk2 in fachDif || !checkKlasse(lk2)) {
                    this.lk2 = null
                    this.wahlzeile = -1
                }
                if (pf3 in fachDif || !checkKlasse(pf3)) {
                    this.pf3 = null
                    this.wahlzeile = -1
                }
                if (pf4 in fachDif || !checkKlasse(pf4)) {
                    this.pf4 = null
                    this.wahlzeile = -1
                }
                if (pf5 in fachDif || !checkKlasse(pf5)) {
                    this.pf5 = null
                }
                updatePflichtfaecher()
            }
    }


    /**
     * Fügt wenn nötig die Pflichtfächer in die Grundkurse ein
     */
    fun updatePflichtfaecher() {
        lock()
        gks =
            gks + pflichtfaecher.filter { (key, value) -> key !in _pfs!! && gks[key].let { it == null || it in value } }
        unlock()
    }

    /**
     * Läd ein LUSD-Formular und exportiert diese Kurswahl mit ihm
     */
    @Throws(InvalidPasswordException::class, IOException::class)
    fun exportPDF(`in`: File, out: File, fachData: FachData) {
        val fachIdMap = fachData.faecher.reversed().associateBy { it.lusdId }
        val felder = fachIdMap.values.associateWith { FeldZeile() }

        val doc = PDDocument.load(`in`)

//        val felder = fachData.faecher.reversed().associate { it.lusdId to FeldZeile() }
        for (feld in doc.documentCatalog.acroForm.fields) {
            when (val name = feld.fullyQualifiedName) {
                SCHUELER_ID_FIELD, JAHRGANG_ID_FIELD -> {}
                PF_5_TYP_FIELD -> feld.setValue(pf5_typ.lusdId)
                else -> {
                    feld.checked = false
                    val parts = name.split('$')
                    val id = parts[3].toInt()
                    val zeile = felder[fachIdMap[id]]!!
//                    val zeile = felder[id]!!
                    when (parts.last()) {
                        "LK1_0" -> zeile.lk1 = feld
                        "LK2_0" -> zeile.lk2 = feld
                        "LK3_0" -> zeile.lk3 = feld
                        "PF3_0" -> zeile.pf3 = feld
                        "PF4_0" -> zeile.pf4 = feld
                        "PK5_0" -> zeile.pk5 = feld
                        "Q1_0" -> zeile.q1 = feld
                        "Q2_0" -> zeile.q2 = feld
                        "Q3_0" -> zeile.q3 = feld
                        "Q4_0" -> zeile.q4 = feld
                    }
                }
            }
        }


        felder[lk1]!!.checkLK1()
        felder[lk2]!!.checkLK2()
//            felder[lk3]!!.checkLK3()
        felder[pf3]!!.checkPF3()
        felder[pf4]!!.checkPF4()
        felder[pf5]!!.checkPK5()

        /* ChatGPT hat gesagt, dass sei langsamer als andersherum
        felder.forEach { (fach, zeile) ->
            zeile.clear()
            gks[fach]?.let {
                zeile.wahlmoeglichkeit = it
            }
        }*/

        for ((fach, wm) in gks) {
            felder[fach]!!.wahlmoeglichkeit = wm
        }

        try {
            doc.save(out)
        } catch (e: Exception) {
            throw e
        } finally {
            doc.close()
        }
    }

    fun toFilename(): String = "${vorname}_$nachname"
        .replace(Regex("[\\\\/:*?\"<>|.&$]"), "")
        .replace(' ', '_')
}
