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

import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * Beschreibt ein Fach
 *
 * @constructor Erstellt ein Fach
 * @property name der Anzeigename des Fachs
 * @property kuerzel eindeutiges Schlüsselattribut für dieses Fachs;
 * Wird verwendet um Instanzen zu vergleichen
 * @property aufgabenfeld das Aufgabenfeld, dem das Fach angehört
 * @property isLk ob das [Fach] als LK gewählt werden kann
 * @property isFremdsprache ob das [Fach] eine Fremdsprache ist
 * @property isKurs ob das Fach als Kurs gewählt werden kann
 * @property brauchtWPF ob SuS das [Fach] as WPF belegt haben müssen
 * @property nurPf4_5 ob das [Fach] nur als 4./5. PF gewählt werden kann
 * @property nurIn beschränkt, in welchen Semestern das Fach gewählt werden kann
 * @property nurFuer bestimmt welche Klassen das Fach wählen können
 * @property isExtra [Fach] zählt nicht zum maximalen Kurse-pro-Semester Zähler
 */
@JsonSerialize(using = FachSerializer::class, keyUsing = FachKeySerializer::class)
data class Fach(
    val name: String,
    val kuerzel: String,
    val aufgabenfeld: Int,
    val isLk: Boolean = false,
    val isFremdsprache: Boolean = false,
    val isKurs: Boolean = true,
    val brauchtWPF: Boolean = false,
    val nurPf4_5: Boolean = false,
    val nurIn: Wahlmoeglichkeit = Wahlmoeglichkeit.DURCHGEHEND,
    val nurFuer: Set<String>? = null,
    val isExtra: Boolean = false
) {
    /**
     * Gibt den Namen des [Fach]s mit, wenn vorhanden, dem Aufgabenfeld zurück
     *
     * Nach dem Schema: `<Fach.name> (<Fach.aufgabenfeld>)`
     */
    fun nameFormatted(): String = if (aufgabenfeld < 1) name else "$name ($aufgabenfeld)"
//    fun nameFormatted(): String = if (aufgabenfeld < 1) name else "$name ${Char(9311+aufgabenfeld)}"

    override fun equals(other: Any?): Boolean = this === other || (other is Fach && this.kuerzel == other.kuerzel)

    override fun hashCode(): Int = kuerzel.hashCode() // nimmt an, dass das selbe Kürzel nur 1x vorkommt

    /** Überprüft ob das Fach mit der gegebenen Klasse gewählt werden kann */
    fun checkKlasse(klasse: String?) = nurFuer?.contains(klasse) != false
}
