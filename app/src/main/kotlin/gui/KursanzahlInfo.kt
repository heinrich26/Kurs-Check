/*
 * Copyright (c) 2022-2024  Hendrik Horstmann
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

package com.kurswahlApp.gui

import com.kurswahlApp.data.Consts
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

/**
 * Label, das verwendet wird um die Kursanzahl/Semester zu limitieren
 */
class KursanzahlInfo(private val semesterkurse: Array<Int>) : JLabel("", RegelLabel.validIcon, LEADING) {
    private val validText =
        ("Wähle in " + (if (semesterkurse.toSet().size == 1) "jedem Semester maximal ${semesterkurse[0]}"
        else "Q1 maximal %d, in Q2 %d, in Q3 %d und in Q4 %d".format(*semesterkurse)) + " Kurse! (Ausgenommen lila-hinterlegte Kurse)").wrappable()

    init {
        border = EmptyBorder(2, 4, 2, 0)
        text = validText
        foreground = Consts.COLOR_VALID
    }

    /** Überprüfen, dass pro Semester die maximale Kurszahl nicht überschritten wird */
    fun match(semesterkurseUser: IntArray): Boolean = semesterkurseUser.zip(semesterkurse).map { it.first - it.second }
        .mapIndexedNotNull { i, n -> if (n > 0) "in Q${i + 1} $n" else null }.let {
            return@let it.isEmpty().also { valid ->
                if (valid) {
                    icon = RegelLabel.validIcon
                    text = validText
                    foreground = Consts.COLOR_VALID
                } else {
                    text = ("Du hast zu viele Kurse! Wähle " + when (it.size) {
                        1 -> it[0]
                        2 -> "${it[0]} & ${it[1]}"
                        3 -> "${it[0]}, ${it[1]} & ${it[2]}"
                        else /* 4 */ -> "${it[0]}, ${it[1]}, ${it[2]} & ${it[3]}"
                    } + " Kurse weniger! (Ausgenommen Extrafächer auf anderen Schienen)").wrappable()
                    foreground = Consts.COLOR_ERROR
                    icon = RegelLabel.errorIcon
                }
            }
        }
}