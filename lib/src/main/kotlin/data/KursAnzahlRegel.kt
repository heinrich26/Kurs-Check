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

/**
 * Objekt das verwendet wird um die Kursanzahl/Semester zu limitieren
 */
// FIXME: 31.08.2022 in ein einzelnes Label packen um den provisorisch hinzugefügten Krams aus Regel, RegelLabel etc. wieder zu entfernen! 
class KursAnzahlRegel(private val semesterkurse: Array<Int>) : Regel("Wähle in " + if (semesterkurse.toSet().size == 1) "jedem Semester maximal ${semesterkurse[0]}"
    else "Q1 maximal %d, in Q2 %d, in Q3 %d und in Q4 %d"
        .format(*semesterkurse) + " Kurse! (Ausgenommen Extra Kurse)", null
) {
    override fun match(data: KurswahlData): Boolean =
        // Überprüfen, dass pro Semester die maximale Kurszahl nicht überschritten wird
        data.countCourses(true).zip(semesterkurse).map { it.first - it.second }
            .mapIndexedNotNull { i, n -> if (n > 0) "in Q${i + 1} $n" else null }.let {
                return@let (it.isEmpty()).also { valid ->
                    if (!valid) errorMsg = "Du hast zu viele Kurse! Wähle " + when (it.size) {
                        1 -> it[0]
                        2 -> "${it[0]} & ${it[1]}"
                        3 -> "${it[0]}, ${it[1]} & ${it[2]}"
                        else /* 4 */ -> "${it[0]}, ${it[1]}, ${it[2]} & ${it[3]}"
                    } + " Kurse weniger! (Ausgenommen Extrafächer auf anderen Schienen)"

                }
            }
}