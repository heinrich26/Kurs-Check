package gui

import add
import data.KurswahlData
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator

class WahlVisualizer(val data: KurswahlData) : JPanel() {
    init {
        layout = GridBagLayout()
        border = RoundedBorder(16)
        add(JLabel("Übersicht"), row = 0, column = 0, columnspan = 2, anchor = GridBagConstraints.WEST)
        add(
            JSeparator(),
            column = 0,
            row = 1,
            columnspan = 3,
            fill = GridBagConstraints.BOTH,
            margin = Insets(6, 0, 0, 0)
        )

        if (data.lk1 != null) {
            add(JLabel("LK 1"), row = 2, column = 0, anchor = GridBagConstraints.WEST, margin = Insets(6, 0, 0, 0))
            add(
                JLabel(data.lk1!!.nameFormatted()),
                row = 2,
                column = 1,
                anchor = GridBagConstraints.WEST,
                margin = Insets(6, 0, 0, 0)
            )
            if (data.lk2 != null) {
                add(JLabel("LK 2"), row = 3, column = 0, anchor = GridBagConstraints.WEST, margin = Insets(2, 0, 0, 0))
                add(
                    JLabel(data.lk2!!.nameFormatted()),
                    row = 3,
                    column = 1,
                    anchor = GridBagConstraints.WEST,
                    margin = Insets(2, 0, 0, 0)
                )
            }
        }
        if (data.pf3 != null) {
            add(JLabel("PF 3"), row = 4, column = 0, anchor = GridBagConstraints.WEST, margin = Insets(2, 0, 0, 0))
            add(
                JLabel(data.pf3!!.nameFormatted()),
                row = 4,
                column = 1,
                anchor = GridBagConstraints.WEST,
                margin = Insets(2, 0, 0, 0)
            )
            if (data.pf4 != null) {
                add(
                    JLabel("PF 4"),
                    row = 5,
                    column = 0,
                    anchor = GridBagConstraints.WEST,
                    margin = Insets(2, 0, 0, 0)
                )
                add(
                    JLabel(data.pf4!!.nameFormatted()),
                    row = 5,
                    column = 1,
                    anchor = GridBagConstraints.WEST,
                    margin = Insets(2, 0, 0, 0)
                )
                if (data.pf5 != null) {
                    add(
                        JLabel("PF 5"),
                        row = 6,
                        column = 0,
                        anchor = GridBagConstraints.WEST,
                        margin = Insets(2, 0, 0, 0)
                    )
                    add(
                        JLabel(data.pf5!!.nameFormatted()),
                        row = 6,
                        column = 1,
                        anchor = GridBagConstraints.WEST,
                        margin = Insets(2, 0, 0, 0)
                    )
                    add(
                        JLabel("(${data.pf5_typ.repr})"),
                        row = 7,
                        column = 1,
                        anchor = GridBagConstraints.WEST,
                        margin = Insets(0, 0, 1, 0)
                    )
                }
            }
        }

        var i = 8
        for ((fach, _) in data.gks) {
            add(
                JLabel(fach.nameFormatted()),
                row = i,
                column = 1,
                anchor = GridBagConstraints.WEST,
                margin = Insets(1, 0, 1, 0)
            )
            i++
        }

        add(
            JSeparator(),
            row = i++,
            columnspan = 3,
            fill = GridBagConstraints.HORIZONTAL,
            margin = Insets(4, 0, 6, 0)
        )

        add(JLabel("Anzahl Kurse"), row = i, anchor = GridBagConstraints.WEST)
        val courseCounts = data.countCourses()
        for ((j, courses) in courseCounts.withIndex()) {
            add(JLabel("Q${j + 1}"), row = i + j, column = 1, anchor = GridBagConstraints.WEST)
            add(JLabel(courses.toString().padStart(2, ' ')), row = i + j, column = 2, anchor = GridBagConstraints.WEST)
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
            anchor = GridBagConstraints.WEST
        )

        i += 5
        add(
            JSeparator(),
            row = i++,
            columnspan = 3,
            fill = GridBagConstraints.HORIZONTAL,
            margin = Insets(4, 0, 6, 0)
        )

        add(
            JLabel("<html>Wochenstunden<br>p. Schuljahr</html>"),
            row = i,
            rowspan = 2,
            anchor = GridBagConstraints.WEST,
            margin = Insets(0, 0, 0, 6)
        )
        add(JLabel("Q1/Q2"), row = i, column = 1, anchor = GridBagConstraints.WEST)
        add(JLabel("Q3/Q4"), row = i + 1, column = 1, anchor = GridBagConstraints.WEST)
        val wochenstunden = data.weeklyCourses()
        add(JLabel(wochenstunden.first.toString()), row = i, column = 2, anchor = GridBagConstraints.WEST)
        add(JLabel(wochenstunden.second.toString()), row = i + 1, column = 2, anchor = GridBagConstraints.WEST)
    }
}