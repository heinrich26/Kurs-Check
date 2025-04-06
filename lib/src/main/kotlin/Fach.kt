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

import com.fasterxml.jackson.annotation.JsonValue
import org.intellij.lang.annotations.Language

/**
 * Beschreibt ein Fach
 *
 * @constructor Erstellt ein [Fach]
 * @property name Anzeigename des [Fachs][Fach].
 * @property kuerzel eindeutiges Schlüsselattribut für dieses [Fachs][Fach];
 * Wird verwendet um Instanzen zu vergleichen.
 * @property aufgabenfeld das Aufgabenfeld, dem das [Fach] angehört.
 * @property isLk ob das [Fach] als LK gewählt werden kann.
 * @property isGk ob das [Fach] als Grundkurs angeboten wird.
 * @property isFremdsprache ob das [Fach] eine Fremdsprache ist.
 * @property isKurs ob das [Fach] als Kurs gewählt werden kann .
 * @property isPf ob das [Fach] als Prüfungsfach (3/4) gewählt werden.
 * @property isExtra ob das [Fach] nicht zum maximalen Kurse-pro-Semester Zähler zählt.
 * @property brauchtWPF ob SuS das [Fach] as WPF belegt haben müssen.
 * @property nurPf4_5 ob das [Fach] nur als 4./5. PF gewählt werden kann.
 * @property nurIn beschränkt, in welchen Semestern das [Fach] gewählt werden kann.
 * @property nurFuer bestimmt welche Klassen dieses [Fach] wählen können.
 * @property lusdId Id des [Fachs][Fach] im LUSD-System.
 * @property infoText über das [Fach], zur Hilfe in der Grundkurs-Übersicht.
 * @property blockAsPf ob das [Fach] als Prüfungsfach automatisch [Wahlmoeglichkeit.DURCHGEHEND] gewählt (geblockt)
 * wird. Nützlich um §47.4 umzusetzen.
 */
@Suppress("PropertyName")
class Fach(
    val name: String,
    @JsonValue val kuerzel: String,
    val aufgabenfeld: Int,
    val isLk: Boolean = false,
    val isGk: Boolean = true,
    val isFremdsprache: Boolean = false,
    isKurs: Boolean = true,
    isPf: Boolean = true,
    val isExtra: Boolean = false,
    val brauchtWPF: Boolean = false,
    val nurPf4_5: Boolean = false,
    val nurIn: Wahlmoeglichkeit = Wahlmoeglichkeit.DURCHGEHEND,
    val nurFuer: Set<String>? = null,
    val lusdId: Int = -1,
    @Language("html") val infoText: String? = null,
    val blockAsPf: Boolean = true
) {
    // Stellt sicher, dass Zusatzkurse nicht als Prüfungsfächer belegt werden können.
    val isPf = isPf && aufgabenfeld >= 0

    val isKurs = isKurs && (isLk || isGk)

    /**
     * Gibt den Namen des [Fachs][Fach], mit sofern vorhanden, dem Aufgabenfeld zurück.
     *
     * Nach dem Schema: [`Name`][Fach.name]` (`[`Aufgabenfeld`][Fach.aufgabenfeld]`)`
     */
    fun nameFormatted(): String = if (aufgabenfeld < 1) name else "$name ($aufgabenfeld)"

    override fun equals(other: Any?): Boolean = this === other || (other is Fach && this.kuerzel == other.kuerzel)

    override fun hashCode(): Int = kuerzel.hashCode() // nimmt an, dass das selbe Kürzel nur 1x vorkommt

    override fun toString(): String = listOf(
        "name='$name'",
        "kuerzel='$kuerzel'",
        "aufgabenfeld=$aufgabenfeld",
        "isLk=$isLk",
        "isGk=$isGk",
        "isFremdsprache=$isFremdsprache",
        "isKurs=$isKurs",
        "isPf=$isPf",
        "isExtra=$isExtra",
        "brauchtWPF=$brauchtWPF",
        "nurPf4_5=$nurPf4_5",
        "nurIn=$nurIn",
        "nurFuer=$nurFuer",
        "lusdId=$lusdId",
        "infoText='$infoText'",
        "blockAsPf=$blockAsPf"
    ).joinToString(prefix = "Fach(", postfix = ")")

    /** Überprüft ob das Fach mit der gegebenen Klasse gewählt werden kann. */
    fun checkKlasse(klasse: String?): Boolean = nurFuer?.contains(klasse) != false

    /** Überprüft, ob dieses Fach mit den gegebenen Wahlpflichfächern gewählt werden kann. */
    fun checkWpf(wpfs: WPFs): Boolean = !brauchtWPF || (wpfs != null && this in wpfs)
}
