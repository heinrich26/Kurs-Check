package com.kurswahlApp.gui

import com.kurswahlApp.add
import com.kurswahlApp.data.Fach
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import com.kurswahlApp.data.Wahlmoeglichkeit
import com.kurswahlApp.wrapHtml
import com.kurswahlApp.wrapTags
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.TitledBorder

class AusgabeLayout(private val fachData: FachData, wahlData: KurswahlData) : JPanel(GridBagLayout()) {
    private val checkboxArray = ArrayList<AusgabeCheckBox>()

    private fun fachPos(fach: Fach) = fachData.faecher.indexOf(fach)

    init {
        preferredSize = Dimension(614, 874)
        minimumSize = preferredSize
        add(JLabel("Übersichtsplan".wrapTags("html", "h2")), row = 0, column = 0, columnspan = 2)


        val checkboxPanel = JPanel(GridBagLayout())
        // fremdsprachen & wpfs holen
        val fs = wahlData.fremdsprachen.map { it.first }
        val wpfs = wahlData.wpfs


        var feldPanel: JPanel? = null
        var feld = Int.MAX_VALUE
        for ((i, fach) in fachData.faecher.withIndex()) {
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

                // Wenn man das Label anklickt wird die ganze Zeile ausgewählt
                feldPanel!!.add(label, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL, weightx = 1.0)
            }

            // Checkbox bauen und hinzufügen
            for (j in 1..4) {
                val box = AusgabeCheckBox()

                if (cond) {
                    box.isOpaque = false // checkboxen haben hässlichen hintergrund, wenn ungesetzt
                } else box.isVisible = false

                checkboxArray.add(box)
                feldPanel!!.add(box, row = i, column = j, margin = Insets(4, 4, 4, 4))
            }
        }
        checkboxPanel.add(feldPanel!!, row = 5, column = 0, fill = GridBagConstraints.BOTH, weightx = 1.0)

        for ((gk, choice) in wahlData.gks) { // auch für wahlData.pfs
            val pos = fachPos(gk)
            val acti = when (choice) {
                Wahlmoeglichkeit.ERSTES_ZWEITES -> listOf(true, true, false, false)
                Wahlmoeglichkeit.ERSTES_DRITTES -> listOf(true, true, true, false)
                Wahlmoeglichkeit.ZWEITES_VIERTES -> listOf(false, true, true, true)
                Wahlmoeglichkeit.DRITTES_VIERTES -> listOf(false, false, true, true)
                Wahlmoeglichkeit.DURCHGEHEND -> listOf(true, true, true, true)
            }
            for (k in 0..3) {
                if (acti[k]) {
                    checkboxArray[k + (pos * 4)].style = AusgabeCheckBox.STYLE.NORMAL
                }
            }
        }

        for (pos in wahlData.lks.filterNotNull().map { fachPos(it) }) {
            for (k in pos * 4..pos * 4 + 3)
                checkboxArray[k].style = AusgabeCheckBox.STYLE.LK
        }

        fachPos(wahlData.pf3!!).let {
            for (k in it * 4..it * 4 + 3)
                checkboxArray[k].style = AusgabeCheckBox.STYLE.PF3
        }

        fachPos(wahlData.pf4!!).let {
            for (k in it * 4..it * 4 + 3)
                checkboxArray[k].style = AusgabeCheckBox.STYLE.PF4
        }

        fachPos(wahlData.pf5!!).let {
            for (k in it * 4..it * 4 + 3)
                checkboxArray[k].style = AusgabeCheckBox.STYLE.PF5
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
        feldPanel.border =
            TitledBorder(RoundedBorder(8), "Anzahl Kurse")

        // Kursanzahlen
        val anzahlen = wahlData.countCourses()
        feldPanel.add(Box.createVerticalStrut(1), column = 0, weightx = 1.0)
        for ((i, n) in anzahlen.withIndex()) {
            feldPanel.add(JLabel("Q${i + 1}", JLabel.LEFT), row = i, column = 1, anchor = GridBagConstraints.WEST)
            feldPanel.add(JLabel("$n".wrapHtml("b", "font-size: 10px").wrapHtml()), row = i, column = 2)
        }
        feldPanel.add(JLabel("gesammt   ", JLabel.LEFT), row = 4, column = 1, anchor = GridBagConstraints.WEST)
        feldPanel.add(JLabel("${anzahlen.sum()}".wrapHtml("b", "font-size: 10px").wrapHtml()), row = 4, column = 2)

        infoPanel.add(feldPanel)

        add(infoPanel, row = 1, column = 2, anchor = GridBagConstraints.NORTH, margin = Insets(4, 0, 0, 0))
    }
}