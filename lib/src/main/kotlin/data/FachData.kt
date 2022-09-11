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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.IOException

/**
 * Hält alle statischen Daten für die Kurswahl
 *
 * Daten werden aus der `dataStruct.json` gezogen oder vom Webserver geladen
 *
 * Neue Felder müssen den selben Namen wie in der `JSON` haben,
 * mit dem Finalen Typen im Primary Constructor hinzugefügt und mit dem eingelesenen Typ
 * (so wies in der JSON steht) in der JSON-Factory deklariert (u. ggf. umgeformt) werden.
 *
 * @property schulId Eindeutiger Identifikator der Schule für diese JSON
 * @property jsonVersion (major-, sub-) Version der FachData-JSON
 * @property faecherMap Ein Mapping der Kürzel zu den dazugehörigen Fächern
 * @property pflichtfaecher Alle Fächer die gewählt werden müssen -
 * werden standardmäßig gesetzt und können nicht abgewählt werden
 * @property wpfs Alle angebotenen Wahlpflichtfächer
 * @property regeln Bestimmen welche weiteren Fächer der/die Schüler*in wählen muss
 * @property wahlzeilen Stellen die möglichen Kombinationen von Prüfungsfächern dar
 * @property wildcards Zusammenfassungen einzelner Kurse unter einem Kürzel
 * @property minKurse Mindestanzahl an Kursen die der/die Schüler*in wählen muss
 * @property maxKurse Maximalanzahl an Kursen die der/die Schüler*in wählen kann
 * @property pf3_4AusschlussFaecher Fächer, von denen nur 1 als  3./4. PF gewählt werden darf (§ 23,6 VO-GO)
 * @property zweiWPFs  Bestimmt ob die Schüler*innen 2 oder nur 1 WPF wählen müssen
 * (Die Option 2 zu wählen bleibt, damit Wechselnde bestimmte Fächer wählen können!)
 * @property semesterkurse Legt fest, wie viele Kurse je Semester maximal belegt werden dürfen (4 Semester => 4 Zahlen)
 * @property schultyp Bestimmt welcher Schultyp vorliegt
 */
@Suppress("unused")
@JsonIncludeProperties(
    "schulId", "jsonVersion", "faecher", "pflichtfaecher", "wpfs", "regeln", "wahlzeilen", "wildcards",
    "minKurse", "maxKurse", "pf3_4AusschlussFaecher", "zweiWPFs", "semesterkurse", "schultyp"
)
class FachData(
    val schulId: String,
    val jsonVersion: Pair<Int, Int>,
    val faecherMap: Map<String, Fach>,
    val pflichtfaecher: Map<Fach, Wahlmoeglichkeit>,
    val wpfs: List<Fach>,
    val regeln: List<Regel>,
    val wahlzeilen: Map<Int, Wahlzeile>,
    val wildcards: Map<String, List<Fach>>,
    val wzWildcards: Map<String, List<String>>,
    val minKurse: Int,
    val maxKurse: Int,
    val pf3_4AusschlussFaecher: Set<String>,
    val zweiWPFs: Boolean,
    val semesterkurse: Array<Int>,
    val schultyp: Schultyp
) {
    val faecher: List<Fach> = faecherMap.values.toList()
    val fremdsprachen: List<Fach> = faecher.filter { it.isFremdsprache }

    val lk1Moeglichkeiten = LinkedHashSet<String>().apply {
        for (wz in wahlzeilen.values) {
            if (wz.lk1.startsWith("$"))
                this.addAll(wzWildcards[wz.lk1]!!)
            else this.add(wz.lk1)
        }
    }.map { faecherMap[it]!! }
    val lk2Moeglichkeiten = LinkedHashSet<String>().apply {
        for (wz in wahlzeilen.values) {
            if (wz.lk2.startsWith("$"))
                this.addAll(wzWildcards[wz.lk2]!!)
            else this.add(wz.lk2)
        }
    }.map { faecherMap[it]!! }


    init {
        // Regeln initialisieren
        regeln.forEach { it.fillData(this) }
    }


    /**
     * Gibt die die LKs zurück
     */
    val lks: List<Fach>
        get() = faecher.filter { it.isLk }

    private val wildcardMapping =
        faecher.associateWith { wildcards.filter { wCard -> it in wCard.value }.keys + it.kuerzel }

    val wzWildcardMapping =
        faecher.associateWith { (wzWildcards.filterValues { value -> it.kuerzel in value }.keys + it.kuerzel) + "*" }

    /**
     * Gibt alle möglichen Wahlzeilen für die gegebenen LKs zurück
     */
    fun filterWahlzeilen(
        lk1: Fach?,
        lk2: Fach?
    ): Map<Int, Wahlzeile> {
        if (lk1 == null || lk2 == null) return wahlzeilen

        return wahlzeilen.filterValues {
            (it.lk1 in wzWildcardMapping[lk1]!! && it.lk2 in wzWildcardMapping[lk2]!!) || (it.lk2 in wzWildcardMapping[lk1]!! && it.lk1 in wzWildcardMapping[lk2]!!)
        }
    }

    /**
     * Läd eine Kurswahl-Datei als [KurswahlData]
     * Die kompatibilität beider Dateiein kann nicht garantiert werden,
     * bei unübereinstimmung der [schulId] wird eine [IllegalArgumentException] ausgegeben!
     */
    @Throws(IOException::class, StreamReadException::class, DatabindException::class, IllegalArgumentException::class)
    fun loadKurswahl(file: File): KurswahlData {
        val mapper = jacksonObjectMapper()
        val injectables = InjectableValues.Std()
        injectables.addValue(FachDataMirror::class.java, FachDataMirror(this))
        mapper.injectableValues = injectables
        return mapper.readValue(file, KurswahlData::class.java)
    }

    /** Erstellt eine leere Kurswahl mit Standardwerten gesetzt! */
    fun createKurswahl(schulId: String): KurswahlData =
        KurswahlData(
            gks = this.pflichtfaecher,
            pflichtfaecher = this.pflichtfaecher,
            readJsonVersion = jsonVersion,
            schulId = schulId
        )

    override fun toString(): String =
        arrayOf(
            "faecher=$faecher",
            "pflichtfaecher=$pflichtfaecher",
            "fremdsprachen=$fremdsprachen",
            "wpfs=$wpfs",
            "regeln=$regeln",
            "wahlzeilen=$wahlzeilen",
            "wildcards=$wildcards",
            "wzWildcards=$wzWildcards",
            "minKurse=$minKurse",
            "maxKurse=$maxKurse",
            "pf3_4AusschlussFaecher=$pf3_4AusschlussFaecher",
            "zweiWPFs=$zweiWPFs",
            "semesterkurse=$semesterkurse",
        ).joinToString(
            ",\n\t",
            "FachData[schulID: $schulId - version ${jsonVersion.first}.${jsonVersion.second}](\n\t",
            "\n)"
        )


    companion object {
        /**
         * Helferklasse um Version einer FachData-JSON zu bestimmen, ohne sie komplett laden zu müssen
         */
        class FachDataInfo(@JsonDeserialize(using = VersionDeserializer::class) @JsonProperty val jsonVersion: Pair<Int, Int>)

        @JvmStatic
        @JsonCreator
        fun fromJson(
            @JsonDeserialize(using = VersionDeserializer::class) @JsonProperty jsonVersion: Pair<Int, Int>,
            @JsonProperty schulId: String,
            @JsonProperty faecher: List<Fach>,
            @JsonProperty pflichtfaecher: Map<String, Wahlmoeglichkeit>,
            @JsonProperty wpfs: List<String>,
            @JsonProperty regeln: List<Regel>,
            @JsonProperty wahlzeilen: Map<Int, Wahlzeile>,
            @JsonProperty wildcards: Map<String, List<String>>,
            @JsonProperty minKurse: Int,
            @JsonProperty maxKurse: Int,
            @JsonProperty pf3_4AusschlussFaecher: Set<String>,
            @JsonProperty zweiWPFs: Boolean,
            @JsonProperty semesterkurse: Array<Int>,
            @JsonProperty schultyp: Schultyp
        ): FachData {
            if (semesterkurse.size != 4) {
                throw IllegalArgumentException("Die Länge von 'semesterkurse' muss exakt 4 sein")
            }

            val wzWildcards = wahlzeilen.flatMap { (_, value) ->
                listOf(value.lk1, value.lk2, value.pf3, value.pf4, value.pf5).filter { it.startsWith('$') }
            }.toSet().associateWith { wildcards[it]!! }

            // Fächer zusätzlich sortieren um auf Aufgabenfelder aufzuteilen
            val faecherMap: Map<String, Fach> =
                faecher.sortedBy { if (it.aufgabenfeld > 0) it.aufgabenfeld else 4 - it.aufgabenfeld }
                    .associateBy { it.kuerzel }
            return FachData(
                schulId = schulId,
                jsonVersion = jsonVersion,
                faecherMap = faecherMap,
                pflichtfaecher = pflichtfaecher.mapKeys { (key: String) -> faecherMap[key]!! },
                wpfs = wpfs.map { faecherMap[it]!! },
                regeln = regeln,
                wahlzeilen = wahlzeilen,
                wildcards = wildcards.mapValues { it.value.map { key -> faecherMap[key]!! } },
                wzWildcards = wzWildcards,
                minKurse = minKurse,
                maxKurse = maxKurse,
                pf3_4AusschlussFaecher = pf3_4AusschlussFaecher,
                zweiWPFs = zweiWPFs,
                semesterkurse = semesterkurse,
                schultyp = schultyp
            )
        }
    }

    /* Außer verwendung, denn Hashcodes sind Buildabhängig und deswegen nicht als
    Versionsindikator tauglich */
    /*override fun hashCode(): Int {
        var result = faecher.toSet().hashCode()
        result = 31 * result + pflichtfaecher.hashCode()
        result = 31 * result + fremdsprachen.toSet().hashCode()
        result = 31 * result + wpfs.toSet().hashCode()
        result = 31 * result + regeln.toSet().hashCode() // ^ gehen
        result = 31 * result + wahlzeilen.hashCode()
        result = 31 * result + wildcards.mapValues { it.value.toSet() }.hashCode()
        result = 31 * result + wzWildcards.mapValues { it.value.toSet() }.hashCode()
        result = 31 * result + minKurse.hashCode()
        result = 31 * result + maxKurse.hashCode()
        result = 31 * result + pf3_4AusschlussFaecher.hashCode()

        return result
    }*/
}