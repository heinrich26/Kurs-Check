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

import java.awt.Color

/**
 * Gruppe unter welcher Fächer zusammengefasst werden können
 * um die maximale Anzahl bestimmter Kurse innerhalb eines Semesters
 * zu limitieren.
 *
 * @property name Anzeigename der Gruppe
 * @property stundenzahl Quadrupel mit den Stundenzahlen für jedes Semester
 * */
class FachGruppe(val name: String, val stundenzahl: Array<Int>) {
    lateinit var color: Color
    init {
        if (stundenzahl.size != 4)
            throw IllegalArgumentException("Die Länge von 'semesterkurse' muss exakt 4 sein")
    }
}