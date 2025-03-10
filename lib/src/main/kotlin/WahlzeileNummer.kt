/*
 * Copyright (c) 2024-2025  Hendrik Horstmann
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
import com.kurswahlApp.data.WahlzeileNummer.Companion.VALUE_OFFSET

/**
 * Helferklasse, welche die Darstellung und Umwandlung von Wahlzeilen-Nummern übernimmt.
 * Um besondere Logik für Leistungskurs-Kombinationen festzulegen sind mitunter zusätzliche
 * [Wahlzeilen][Wahlzeile] nötig. Um weiterhin die gewohnten Nummern beizubehalten, können
 * „verschobene“ Ziffern vergeben werden. Diese werden um [VALUE_OFFSET] inkrementiert. Eine 4
 * kann also mehrfach durch `n * VALUE_OFFSET + 4` dargestellt werden. In der GUI und dem Export
 * werden die [logischen][logical] Werte verwendet - der Nutzer bekommt also nichts mit.
 *
 * Wir nehmen hier an, dass die logische Zahl an Zeilen 512 in absehbarer Zeit nicht überschreitet.
 *
 * @property value Beschreibt den eigentlichen Wert, wird nur intern für Schlüssel und Identifikation genutzt.
 * @property logical Beschreibt die logische Nummer, also im Bereich 0-[VALUE_OFFSET].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class WahlzeileNummer(@get:JsonValue var value: Int) {

    val logical: Int
        get() = getLogical(value)

    /**
     * Versetzt diese Nummer in den Undefinierten Zustand.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun unset() { value = UNDEFINED }

    /**
     * Prüft, ob diese Nummer undefiniert ist.
     */
    fun isUnset() = value == UNDEFINED

    override fun toString(): String = logical.toString()
    override fun equals(other: Any?): Boolean = this === other || other is WahlzeileNummer && other.value == value
    override fun hashCode(): Int = value

    companion object {
        /**
         * Beschreibt den Überlaufpunkt, an welchem eine [WahlzeileNummer] von 0 aus gezählt wird.
         * Die Zeile 4 könnte also auch als `516=512+4, 1028=2*512+4, ...` geschrieben werden.
         *
         * Eine logische Zahl von 512 Zeilen sollte in absehbarer Zeit nicht überschritten werden!
         */
        const val VALUE_OFFSET = 0x200

        /**
         * Wert für eine undefinierte Nummer
         */
        const val UNDEFINED = -1

        /**
         * Berechnet die logische Nummer einer
         * Wahlzeilen-Nummer im Bereich 0-[VALUE_OFFSET].
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun getLogical(actual: Int) = actual % VALUE_OFFSET
    }
}