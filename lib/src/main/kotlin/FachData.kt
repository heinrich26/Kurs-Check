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

package com.kurswahlApp.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kurswahlApp.data.FachData.Companion.fromJson
import java.io.File
import java.io.IOException

/**
 * Hält alle statischen Daten für die Kurswahl
 *
 * Daten werden vom [Webserver](https://github.com/heinrich26/Kurs-Check/blob/data/) geladen
 *
 * Neue Felder müssen den selben Namen wie in der `JSON` haben,
 * mit dem finalen Typen im Primary Constructor hinzugefügt und mit dem eingelesenen Typ
 * (so wies in der JSON steht) in der [Factory][fromJson] deklariert (u. ggf. transformiert) werden.
 *
 * @property schulId Eindeutiger Identifikator der Schule für diese JSON
 * @property jsonVersion (major-, sub-) Version der FachData-JSON
 * @property faecherMap Ein Mapping der Kürzel zu den dazugehörigen Fächern
 * @property pflichtfaecher Alle Fächer die gewählt werden müssen -
 * werden automatisch gewählt und können nicht abgewählt werden
 * @property wpfs Alle angebotenen Wahlpflichtfächer
 * @property regeln Bestimmen welche weiteren Fächer der/die Schüler*in wählen muss
 * @property wahlzeilen Stellen die möglichen Kombinationen von Prüfungsfächern dar
 * @property wildcards Zusammenfassungen einzelner Kurse unter einem Kürzel
 * @property minKurse Mindestanzahl an Kursen die der/die Schüler*in wählen muss
 * @property maxKurse Maximalanzahl an Kursen die der/die Schüler*in wählen kann
 * @property pf3_4AusschlussFaecher Fächer, von denen nur 1 als  3./4. PF gewählt werden darf (§ 23,6 VO-GO)
 * @property zweiWPFs Bestimmt ob die Schüler*innen 2 oder nur 1 WPF wählen müssen
 * (Die Option 2 zu wählen bleibt, damit Wechselnde bestimmte Fächer wählen können!)
 * @property strikteWPFs Bestimmt, ob **ALLE** Kurse zuvor als Wahlpflicht Kurs belegt worden sein müssen.
 * @property semesterkurse Legt fest, wie viele Kurse je Semester maximal belegt werden dürfen (4 Semester => 4 Zahlen)
 * @property klassen Definiert die an der Schule angebotenen Klassen
 * @property schultyp Bestimmt welcher Schultyp vorliegt
 * @property nutztLusd Legt fest, ob LUSD PDFs geladen und gespeichert werden können
 * @property fnamePattern Regex, das die passende LUSD-PDF zum Schülernamen findet.
 * Mögliche Variablen: [%vname%][KurswahlData.vorname] und [%nname%][KurswahlData.nachname]
 * (Leerzeichen werden durch Unterstriche ersetzt)
 * @property umfragen Liste von [Umfragen][UmfrageBase], die den Schüler*innen angezeigt werden
 */
@Suppress("unused", "PropertyName", "LocalVariableName")
@JsonIncludeProperties(
    "schulId", "jsonVersion", "faecher", "pflichtfaecher", "wpfs", "regeln", "wahlzeilen",
    "wildcards", "minKurse", "maxKurse", "pf3_4AusschlussFaecher", "zweiWPFs", "strikteWPFs",
    "semesterkurse", "klassen", "schultyp", "nutztLusd", "fnamePattern", "umfragen"
)
class FachData(
    val schulId: String,
    val jsonVersion: JsonVersion,
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
    val strikteWPFs: Boolean,
    val semesterkurse: Array<Int>,
    val klassen: Set<String> = emptySet(),
    val schultyp: Schultyp,
    val nutztLusd: Boolean = false,
    val fnamePattern: String?,
    val umfragen: List<UmfrageBase<*>> = emptyList()
) {
    val faecher: List<Fach> = faecherMap.values.toList()
    val fremdsprachen: List<Fach> = faecher.filter(Fach::isFremdsprache)

    init {
        // Regeln initialisieren
        regeln.forEach { it.fillData(this) }
    }

    private val wildcardMapping =
        faecher.associateWith { wildcards.filterValues { v -> it in v }.keys + it.kuerzel }

    val wzWildcardMapping =
        faecher.associateWith { wzWildcards.filterValues { v -> it.kuerzel in v }.keys + it.kuerzel + "*" }

    val conflictGroups = regeln.filterIsInstance<KonfliktRegel>().map { wzWildcards[it.wildcard]!! }

    /**
     * Überprüft die Wahlzeilen auf unbekannte Kürzel
     */
    fun checkWahlzeilen() {
        val undefined = wahlzeilen
            .flatMap { (_, wz) -> wz.toList() }
            .toSet().minus("*")
            .filter { !it.startsWith('$') && it !in faecherMap.keys }

        require(undefined.isEmpty()) { "Undefinierte(s) Kürzel $undefined in den Wahlzeilen!" }
    }


    /**
     * Bereitet einen [ObjectMapper][com.fasterxml.jackson.databind.ObjectMapper]
     * für's laden von [KurswahlData]-JSON vor.
     */
    private fun getMapper() = jacksonObjectMapper().also {
        it.injectableValues = InjectableValues.Std().addValue(FachDataMirror::class.java, FachDataMirror(this))
    }

    /**
     * Läd eine Kurswahl-Datei als [KurswahlData]
     * Die Kompatibilität beider Dateiein kann nicht garantiert werden,
     * bei Unübereinstimmen der [schulId] wird eine [IllegalArgumentException] ausgegeben!
     */
    @Throws(IOException::class, StreamReadException::class, DatabindException::class, IllegalArgumentException::class)
    fun loadKurswahl(file: File): KurswahlData = getMapper().readValue<KurswahlData>(file)

    /**
     * Läd einen String als [KurswahlData]
     * Die Kompatibilität beider Dateiein kann nicht garantiert werden,
     * bei Unübereinstimmen der [schulId] wird eine [IllegalArgumentException] ausgegeben!
     */
    @Throws(IOException::class, StreamReadException::class, DatabindException::class, IllegalArgumentException::class)
    fun loadKurswahl(s: String): KurswahlData = getMapper().readValue<KurswahlData>(s)

    /** Erstellt eine leere Kurswahl mit Standardwerten gesetzt! */
    fun createKurswahl(): KurswahlData =
        KurswahlData(
            gks = this.pflichtfaecher,
            pflichtfaecher = this.pflichtfaecher,
            readJsonVersion = this.jsonVersion,
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
            "semesterkurse=${semesterkurse.asList()}",
            "klassen=$klassen",
            "schultyp=$schultyp",
            "nutztLUSD=$nutztLusd",
            "fnamePattern=$fnamePattern",
            "umfragen=$umfragen"
        ).joinToString(
            ",\n\t",
            "FachData[schulID: $schulId - v$jsonVersion](\n\t",
            "\n)"
        )


    companion object {
        /**
         * Helferklasse um Version einer FachData-JSON zu bestimmen, ohne sie komplett laden zu müssen
         */
        class FachDataInfo(@JsonProperty val jsonVersion: JsonVersion)

        @JvmStatic
        @JsonCreator
        fun fromJson(
            @JsonProperty jsonVersion: JsonVersion,
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
            @JsonProperty strikteWPFs: Boolean,
            @JsonProperty semesterkurse: Array<Int>,
            @JsonProperty klassen: Set<String> = emptySet(),
            @JsonProperty schultyp: Schultyp,
            @JsonProperty nutztLusd: Boolean,
            @JsonProperty fnamePattern: String?,
            @JsonProperty umfragen: List<UmfrageBase<*>> = emptyList()
        ): FachData {
            require (semesterkurse.size == 4) { "Die Länge von 'semesterkurse' muss exakt 4 sein" }

            val wzWildcards = (wahlzeilen.flatMap { (_, value) ->
                listOf(value.lk1, value.lk2, value.pf3, value.pf4, value.pf5).filter { it.startsWith('$') }
            } + regeln.filterIsInstance<KonfliktRegel>().map { it.wildcard }).toSet().associateWith(wildcards::getValue)

            // Fächer zusätzlich sortieren um auf Aufgabenfelder aufzuteilen
            val faecherMap: Map<String, Fach> =
                faecher.sortedBy { if (it.aufgabenfeld > 0) it.aufgabenfeld else 4 - it.aufgabenfeld }
                    .associateBy(Fach::kuerzel)
            return FachData(
                schulId = schulId,
                jsonVersion = jsonVersion,
                faecherMap = faecherMap,
                pflichtfaecher = pflichtfaecher.mapKeys { faecherMap.getValue(it.key) },
                wpfs = wpfs.map(faecherMap::getValue),
                regeln = regeln,
                wahlzeilen = wahlzeilen,
                wildcards = wildcards.mapValues { it.value.map(faecherMap::getValue) },
                wzWildcards = wzWildcards,
                minKurse = minKurse,
                maxKurse = maxKurse,
                pf3_4AusschlussFaecher = pf3_4AusschlussFaecher,
                zweiWPFs = zweiWPFs,
                strikteWPFs = strikteWPFs,
                semesterkurse = semesterkurse,
                klassen = klassen,
                schultyp = schultyp,
                nutztLusd = nutztLusd,
                fnamePattern = fnamePattern,
                umfragen = umfragen
            )
        }
    }

    /* TODO Hashcode implementieren:
        Außer Verwendung, denn Hashcodes sind Buildabhängig und deswegen nicht als
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