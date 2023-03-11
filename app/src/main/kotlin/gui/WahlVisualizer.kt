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

package com.kurswahlApp.gui

import com.kurswahlApp.data.KurswahlData
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.border.TitledBorder

class WahlVisualizer(val data: KurswahlData) : JPanel(GridBagLayout()) {
    init {
        border = TitledBorder(RoundedBorder(16), "Übersicht").apply { titleFont = titleFont.deriveFont(Font.BOLD, 13f) }

        if (data.lk1 != null) {
            add(JLabel("1. LK"), row = 2, column = 0, anchor = GridBagConstraints.WEST, margin = Insets(top = 6))
            add(
                JLabel(data.lk1!!.nameFormatted()),
                row = 2,
                column = 1,
                columnspan = 2,
                anchor = GridBagConstraints.WEST,
                margin = Insets(top = 6)
            )
            if (data.lk2 != null) {
                add(JLabel("2. LK"), row = 3, column = 0, anchor = GridBagConstraints.WEST, margin = Insets(top = 2))
                add(
                    JLabel(data.lk2!!.nameFormatted()),
                    row = 3,
                    column = 1,
                    columnspan = 2,
                    anchor = GridBagConstraints.WEST,
                    margin = Insets(top = 2)
                )
            }
        }
        if (data.pf3 != null) {
            add(JLabel("3. PF"), row = 4, column = 0, anchor = GridBagConstraints.WEST, margin = Insets(top = 2))
            add(
                JLabel(data.pf3!!.nameFormatted()),
                row = 4,
                column = 1,
                columnspan = 2,
                anchor = GridBagConstraints.WEST,
                margin = Insets(top = 2)
            )
            if (data.pf4 != null) {
                add(
                    JLabel("4. PF"),
                    row = 5,
                    column = 0,
                    anchor = GridBagConstraints.WEST,
                    margin = Insets(top = 2)
                )
                add(
                    JLabel(data.pf4!!.nameFormatted()),
                    row = 5,
                    column = 1,
                    columnspan = 2,
                    anchor = GridBagConstraints.WEST,
                    margin = Insets(top = 2)
                )
                if (data.pf5 != null) {
                    add(
                        JLabel("5. PK"),
                        row = 6,
                        column = 0,
                        anchor = GridBagConstraints.WEST,
                        margin = Insets(top = 2)
                    )
                    add(
                        JLabel(data.pf5!!.nameFormatted()),
                        row = 6,
                        column = 1,
                        columnspan = 2,
                        anchor = GridBagConstraints.WEST,
                        margin = Insets(top = 2)
                    )
                    add(
                        JLabel("(${data.pf5_typ.repr})"),
                        row = 7,
                        column = 1,
                        columnspan = 2,
                        anchor = GridBagConstraints.WEST,
                        margin = Insets(bottom = 1)
                    )
                }
            }
        }

        var i = 8
        for ((fach, _) in data.gks) {
            add(
                JLabel(fach.nameFormatted()),
                row = i++,
                column = 1,
                columnspan = 2,
                anchor = GridBagConstraints.WEST,
                margin = Insets(y = 1)
            )
        }

        add(
            JSeparator(),
            row = i++,
            columnspan = 3,
            fill = GridBagConstraints.HORIZONTAL,
            margin = Insets(top = 4, bottom = 6)
        )

        add(JLabel("Anzahl Kurse"), row = i, anchor = GridBagConstraints.WEST)
        val courseCounts = data.countCourses()
        for ((j, courses) in courseCounts.withIndex()) {
            add(JLabel("Q${j + 1}"), row = i + j, column = 1, anchor = GridBagConstraints.WEST)
            add(JLabel(courses.toString()), row = i + j, column = 2, anchor = GridBagConstraints.EAST)
        }
        val bold = Font(font.name, Font.BOLD, font.size)
        add(
            JLabel("ges. ").apply { this.font = bold },
            row = i + 4,
            column = 1,
            anchor = GridBagConstraints.WEST
        )
        add(
            JLabel(courseCounts.sum().toString()).apply { this.font = bold },
            row = i + 4,
            column = 2,
            anchor = GridBagConstraints.EAST
        )

        i += 5
        add(
            JSeparator(),
            row = i++,
            columnspan = 3,
            fill = GridBagConstraints.HORIZONTAL,
            margin = Insets(top = 4, bottom = 6)
        )

        add(
            JLabel("<html>Wochenstunden<br>pro Schuljahr</html>"),
            row = i,
            rowspan = 2,
            anchor = GridBagConstraints.WEST,
            margin = Insets(right = 6)
        )
        add(JLabel("Q1 & Q2"), row = i, column = 1, anchor = GridBagConstraints.WEST)
        add(JLabel("Q3 & Q4"), row = i + 1, column = 1, anchor = GridBagConstraints.WEST)
        val wochenstunden = data.weeklyCourses()
        add(JLabel(wochenstunden.first.toString()), row = i, column = 2, anchor = GridBagConstraints.EAST)
        add(JLabel(wochenstunden.second.toString()), row = i + 1, column = 2, anchor = GridBagConstraints.EAST)
    }
}