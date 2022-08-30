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

import com.kurswahlApp.data.*
import com.kurswahlApp.data.Wahlmoeglichkeit.*
import java.awt.*
import javax.swing.*


class GrundkursWahl(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData {
        val gks = mutableMapOf<Fach, Wahlmoeglichkeit>()
        val pfs = wahlData.pfs
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (fach in pfs) continue
            // Speichern der gewählten Grundkurse und dessen Semester
            gks[fach] =
                Wahlmoeglichkeit.fromBools(checkboxArray.subList(i * 4, i * 4 + 4).map { it.isSelected }) ?: continue
        }

        return wahlData.copy(gks = gks.toMap())
    }

    override fun isDataValid(): Boolean {
        val pfs = wahlData.pfs
        var counter = 0
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (fach in pfs) {
                counter += 4
                continue
            }
            // Übergabe der gewählten Grundkurse und dessen Semester
            counter += when (checkboxArray.subList(i * 4, i * 4 + 4).map { it.isSelected }) {
                ERSTES_ZWEITES.bools, DRITTES_VIERTES.bools -> 2
                ERSTES_DRITTES.bools, ZWEITES_VIERTES.bools -> 3
                DURCHGEHEND.bools -> 4
                Wahlmoeglichkeit.UNGEWAEHLT_BOOLS -> continue
                else -> return false
            }
        }
        return counter in fachData.minKurse..fachData.maxKurse
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { GrundkursWahl(testKurswahl, testFachdata) }
        }
    }

    private fun checkData(): Boolean {
        val data = close()
        data.lock()
        return fachData.regeln.mapIndexed { i, it ->
            val result = it.match(data)
            regelLabelArray[i].setAppearance(result)
            result
        }.all { it }
    }

    private val checkboxArray = ArrayList<JToggleButton>(fachData.faecher.size * 4)
    private val labelArray = arrayOfNulls<JLabel>(fachData.faecher.size)

    private val regelLabelArray = fachData.regeln.map { RegelLabel(it) }.toTypedArray()

    private var anzahl: Int = 0
        set(value) {
            field = value
            anzahlLabel.text = "$anzahl Kurse"

            anzahlInfoLabel.text = when {
                value < fachData.minKurse -> "Bitte wähle mindestens ${fachData.minKurse} Kurse".wrapHtml(
                    "p",
                    "color:#F92F60"
                ).wrapHtml()
                value > fachData.maxKurse -> "Bitte wähle maximal ${fachData.maxKurse} Kurse".wrapHtml(
                    "p",
                    "color:#F92F60"
                ).wrapHtml()
                else -> "Es wurden genug Kurse gewählt".wrapHtml("p", "color:#00D26A").wrapHtml()
            }
        }

    private val anzahlLabel = JLabel("$anzahl Kurse")
    private val anzahlInfoLabel = JLabel("")

    private val checkboxPanel = JPanel(GridBagLayout())

    private val checkButton = JButton("Überprüfen")

    init {
        add(anzahlLabel, row = 1)
        add(anzahlInfoLabel, row = 1)

        checkButton.addActionListener { checkData() }
        add(checkButton, row = 1, column = 2)

        buildCheckboxes()

        val scrollPane =
            JScrollPane(checkboxPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.preferredSize = Dimension(250, 350)
        add(scrollPane, row = 0, column = 0, columnspan = 2, margin = Insets(0, 0, 6, 0))

        faecherBlocken()

        // Grundkurse (Daten) eintragen
        for ((gk, choice) in wahlData.gks) {
            val pos = fachPos(gk)
            val acti = choice.bools
            for (k in 0..3) {
                if (acti[k]) {
                    checkboxArray[k + (pos * 4)].isSelected = true
                }
            }
        }


        // Kurse zählen
        anzahl = checkboxArray.count { it.isVisible && it.isSelected }


        // Regeln darstellen
        val regelPanel = ScrollablePanel(null)
        regelPanel.layout = BoxLayout(regelPanel, BoxLayout.PAGE_AXIS)
        regelPanel.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT)
        regelLabelArray.forEach { regelPanel.add(it) }

        val scrollPane2 = JScrollPane(regelPanel)
        scrollPane2.preferredSize = Dimension(200, 350)

        checkData()

        add(scrollPane2, row = 0, column = 2, margin = Insets(0, 8, 6, 0))
    }


    /**
     * Erstellt Checkboxen mit Labels und versteckt jene, die der Schüler nicht wählen kann
     */
    private fun buildCheckboxes() {
        // fremdsprachen & wpfs holen
        val fs = wahlData.fremdsprachen.map { it.first }
        val wpfs = wahlData.wpfs

        for ((i, fach) in fachData.faecher.withIndex()) {
            // if A: B else true == !A or B
            val cond: Boolean =
                fach.isKurs &&
                        if (fach.isFremdsprache) fach in fs
                        else (!fach.brauchtWPF || (wpfs != null && (fach == wpfs.first || fach == wpfs.second)))
            // cond == true -> wählbar, sonst versteckt


            if (cond) {
                val label = JLabel(fach.nameFormatted())

                // Wenn man das Label anklickt wird die ganze Zeile ausgewählt
                label.addMouseListener(onClick = {
                    label.foreground = Color.BLACK
                    // Checkboxen der Zeile holen
                    val zeile = checkboxArray.subList(i * 4, i * 4 + 4)

                    // An- und Abwählen
                    when (zeile.count { !it.isEnabled || it.isSelected }) {
                        0, 2, 3 -> {
                            for (box in zeile)
                                if (box.isEnabled && !box.isSelected) {
                                    box.isSelected = true
                                    anzahl++
                                }
                        }
                        else /* 1, 4 */ -> {
                            for (box in zeile)
                                if (box.isEnabled && box.isSelected) {
                                    box.isSelected = false
                                    anzahl--
                                }
                        }
                    }
                }
                )
                labelArray[i] = label
                checkboxPanel.add(label, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL)
            }

            // Checkbox bauen und hinzufügen
            for (j in 1..4) {
                val box = JCheckBox()

                if (cond) {
                    box.isOpaque = false
                    box.addActionListener { _ ->
                        if (box.isSelected) anzahl++ else anzahl--
                        // Das Label Rot färben, wenn die Reihe ungültig ist
                        labelArray[i]!!.foreground =
                            if (checkboxArray.subList(i * 4, i * 4 + 4).map { it.isSelected }.let {
                                    it == listOf(false, false, false, false) ||
                                            Wahlmoeglichkeit.fromBools(it) != null
                                }) Color.BLACK
                            else Consts.COLOR_ERROR
                    }
                } else box.isVisible = false

                checkboxArray.add(box)
                checkboxPanel.add(box, row = i, column = j, fill = GridBagConstraints.HORIZONTAL)
            }
            //Lockt Checkboxen für Fächer die nur in bestimmten Semestern gewählt werden können
            when (fach.nurIn) {
                ERSTES_ZWEITES -> {
                    checkboxArray[checkboxArray.size - 2].isEnabled = false
                    checkboxArray.last().isEnabled = false
                }
                DRITTES_VIERTES -> {
                    checkboxArray[checkboxArray.size - 4].isEnabled = false
                    checkboxArray[checkboxArray.size - 3].isEnabled = false
                }
                else -> {}
            }
        }
    }

    /**
     * Blockt Prüfungs- und Pflichtfächer, damit der Nutzer nichts an der auswahl verändern kann
     */
    private fun faecherBlocken() {
        // Blockt Prüfungsfächer
        for (pos in wahlData.pfs.filterNotNull().map { fachPos(it) }) {
            for (k in pos * 4..pos * 4 + 3)
                checkboxArray[k].let {
                    it.isSelected = true
                    it.isEnabled = false
                }
        }
        // Blockt Pflichtfächer
        for ((pf, _) in fachData.pflichtfaecher) {
            val pos = fachPos(pf)
            for (k in pos * 4..pos * 4 + 3)
                checkboxArray[k].let {
                    it.isSelected = true
                    it.isEnabled = false
                }
        }
    }

    private fun fachPos(fach: Fach) = fachData.faecher.indexOf(fach)

    override val windowName: String
        get() = "Grundkurse"
}

