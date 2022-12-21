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

package gui


import com.kurswahlApp.data.Fach
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import java.awt.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.MatteBorder
import javax.swing.border.TitledBorder

class AusgabeLayout(fachData: FachData, wahlData: KurswahlData) : JPanel(GridBagLayout()) {
    /* TODO Ausgabe sicher machen, sodass es keinen Overflow gibt!
    class AusgabeLayout(fachData: FachData, wahlData: KurswahlData) :
        JPanel(VerticalLayout(alignment = VerticalLayout.LEFT)) {*/
    private val checkboxMap = mutableMapOf<Fach, Array<AusgabeCheckBox>>()

    init {
        preferredSize = Dimension(614, 874) // Din A4
        minimumSize = preferredSize
        add(JLabel("Übersichtsplan".wrapTags("html", "h2")), row = 0, column = 0, columnspan = 2)


        val checkboxPanel = JPanel(GridBagLayout())
        // fremdsprachen & wpfs holen
        val fs = wahlData.fremdsprachen.map { it.first }
        val wpfs = wahlData.wpfs


        var feldPanel: JPanel? = null
        var feld = Int.MAX_VALUE
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (!fach.isKurs) continue

            if (feld > 0 && feld != fach.aufgabenfeld) {
                if (feldPanel != null)
                    checkboxPanel.add(
                        feldPanel,
                        row = feld - 1,
                        column = 0,
                        fill = GridBagConstraints.BOTH,
                        weightx = 1.0
                    )
                feld = fach.aufgabenfeld
                feldPanel = JPanel(GridBagLayout())
                feldPanel.border =
                    TitledBorder(RoundedBorder(8), if (feld > 0) "Aufgabenfeld $feld" else "weitere Fächer")

            }
            // if A: B else true == !A or B
            val cond: Boolean =
                if (fach.isFremdsprache) fach in fs
                else (!fach.brauchtWPF || (wpfs != null && (fach == wpfs.first || fach == wpfs.second)))
            // cond == true -> wählbar, sonst versteckt


            if (cond) {
                val label = JLabel(fach.name)

                feldPanel!!.add(label, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL, weightx = 1.0)

                // Checkboxen bauen und hinzufügen
                checkboxMap[fach] =
                    arrayOf(AusgabeCheckBox(), AusgabeCheckBox(), AusgabeCheckBox(), AusgabeCheckBox()).apply {
                        val insets = Insets(4, 4, 4, 4)
                        forEachIndexed { j, box -> feldPanel!!.add(box, row = i, column = j + 1, margin = insets) }
                    }
            }
        }
        checkboxPanel.add(feldPanel!!, row = 5, column = 0, fill = GridBagConstraints.BOTH, weightx = 1.0)

        for ((gk, choice) in wahlData.gks) { // auch für wahlData.pfs
            val row = checkboxMap[gk]!!
            for ((k, isActive) in choice.bools.withIndex()) {
                if (isActive) {
                    row[k].style = AusgabeCheckBox.STYLE.NORMAL
                }
            }
        }

        for (lk in wahlData.lks.filterNotNull()) {
            for (box in checkboxMap[lk]!!)
                box.style = AusgabeCheckBox.STYLE.LK
        }

        for (box in checkboxMap[wahlData.pf3!!]!!) {
            box.style = AusgabeCheckBox.STYLE.PF3
        }

        for (box in checkboxMap[wahlData.pf4!!]!!) {
            box.style = AusgabeCheckBox.STYLE.PF4
        }

        for (box in checkboxMap[wahlData.pf5!!]!!) {
            box.style = AusgabeCheckBox.STYLE.PF5
        }

        add(checkboxPanel, row = 1, column = 1, margin = Insets(4, 4, 4, 4))


        // Extrainformationen
        // Panel mit allen anderen Infos
        feldPanel = JPanel(GridBagLayout())
        feldPanel.border =
            TitledBorder(RoundedBorder(8), "Deine Infos")

        feldPanel.add(
            JLabel("Form der 5. PK: <b>${wahlData.pf5_typ.repr}</b> ".wrapHtml()),
            row = 0,
            column = 0,
            anchor = GridBagConstraints.LINE_START
        )

        // Eintrittsjahr
        Calendar.getInstance().let {
            val year = it[Calendar.YEAR].let { year ->
                if (it.before(Calendar.Builder().setDate(year, Calendar.JULY, 1).build())) year else year + 1
            }
            feldPanel!!.add(
                JLabel(
                    (year.toString().wrapHtml("h3", "text-align: center") +
                            "Jahr des Eintritts in die<br>gymnasiale Oberstufe").wrapHtml()
                ), row = 0, column = 1, rowspan = 2, margin = Insets(0, 8, 0, 0)
            )
        }

        // Wahlzeile
        feldPanel.add(
            JLabel("Wahlzeile: <b>${wahlData.wahlzeile}</b>".wrapHtml()),
            row = 1,
            column = 0,
            anchor = GridBagConstraints.LINE_START
        )

        feldPanel.add(
            JLabel("Vorname: <b>${wahlData.vorname}</b>".wrapHtml()),
            row = 2,
            column = 0,
            anchor = GridBagConstraints.LINE_START
        )

        feldPanel.add(
            JLabel("Nachname: <b>${wahlData.nachname}</b>".wrapHtml()),
            row = 3,
            column = 0,
            anchor = GridBagConstraints.LINE_START
        )

        feldPanel.add(
            JLabel("geboren: <b>${wahlData.geburtsdatum!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}</b> in <b>${wahlData.geburtsort}</b>".wrapHtml()),
            row = 4,
            column = 0,
            anchor = GridBagConstraints.LINE_START
        )

        feldPanel.add(
            JLabel("Staatsangehörigkeit: <b>${wahlData.staatsangehoerigkeit}</b>".wrapHtml()),
            row = 5,
            column = 0,
            anchor = GridBagConstraints.LINE_START
        )


        val infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.add(feldPanel)

        feldPanel = JPanel(GridBagLayout())
        feldPanel.border = TitledBorder(RoundedBorder(8), "Anzahl Kurse")

        // Kursanzahlen
        val anzahlen = wahlData.countCourses()
        feldPanel.add(Box.createVerticalStrut(1), column = 0, weightx = 1.0)
        for ((i, n) in anzahlen.withIndex()) {
            feldPanel.add(JLabel("Q${i + 1}", JLabel.LEFT), row = i, column = 1, anchor = GridBagConstraints.WEST)
            feldPanel.add(JLabel("$n".wrapHtml("b", "font-size: 10px").wrapHtml()), row = i, column = 2)
        }
        feldPanel.add(JLabel("gesamt   ", JLabel.LEFT), row = 4, column = 1, anchor = GridBagConstraints.WEST)
        feldPanel.add(JLabel("${anzahlen.sum()}".wrapHtml("b", "font-size: 10px").wrapHtml()), row = 4, column = 2)

        infoPanel.add(feldPanel)


        feldPanel = JPanel(GridBagLayout())
        feldPanel.border = TitledBorder(RoundedBorder(8), "Unterschrift")
        feldPanel.add(JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("d.M.yyyy"))).also {
            Color(120, 120, 120).let { gray ->
                it.border = MatteBorder(0, 0, 2, 0, gray)
                it.foreground = gray
            }
            it.font = it.font.deriveFont(12f)
        }, fill = GridBagConstraints.HORIZONTAL, margin = Insets(8, 0, 0, 0), weightx = 1.0)

        infoPanel.add(Box.createVerticalStrut(24))
        infoPanel.add(feldPanel)

        add(infoPanel, row = 1, column = 2, anchor = GridBagConstraints.NORTH, margin = Insets(4, 0, 0, 0))
    }
}