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
import com.kurswahlApp.data.Consts.COLOR_PRIMARY
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

        // Überprüfen, dass pro Semester die maximale Kurszahl nicht überschritten wird
        if (!wahlData.countCourses(true).zip(fachData.semesterkurse).all { it.first <= it.second }) return false

        return counter in fachData.minKurse..fachData.maxKurse
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { GrundkursWahl(testKurswahl, testFachdata) }
        }

        private val extraFachColor =
            Color(COLOR_PRIMARY.colorSpace, COLOR_PRIMARY.getRGBColorComponents(null), .1f)
    }

    private fun checkData(): Boolean {
        val data = close()
        data.lock()
        return regelLabelArray.map {
            it.match(data)
        }.all { it } && kursanzahlInfoLabel.match(semesterkurse)
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
    private val kursanzahlInfoLabel = KursanzahlInfo(fachData.semesterkurse)

    // Labels für die Kurse/Semester
    private val q1AnzahlLabel = JLabel("0")
    private val q2AnzahlLabel = JLabel("0")
    private val q3AnzahlLabel = JLabel("0")
    private val q4AnzahlLabel = JLabel("0")
    private var q1Anzahl = 0
        set(value) {
            field = semesterAnzahlSetter(value, q1AnzahlLabel, 0)
        }
    private var q2Anzahl = 0
        set(value) {
            field = semesterAnzahlSetter(value, q2AnzahlLabel, 1)
        }
    private var q3Anzahl = 0
        set(value) {
            field = semesterAnzahlSetter(value, q3AnzahlLabel, 2)
        }
    private var q4Anzahl = 0
        set(value) {
            field = semesterAnzahlSetter(value, q4AnzahlLabel, 3)
        }
    private val semesterkurse: IntArray
        get() = intArrayOf(q1Anzahl, q2Anzahl, q3Anzahl, q4Anzahl)

    private fun semesterAnzahlSetter(value: Int, label: JLabel, index: Int): Int {
        label.text = value.toString()
        label.foreground = if (fachData.semesterkurse[index] < value) Consts.COLOR_ERROR else Color.BLACK
        return value
    }

    private fun semesterAnzahlIncr(i: Int) = when (i) {
        1 -> q1Anzahl++
        2 -> q2Anzahl++
        3 -> q3Anzahl++
        else -> q4Anzahl++
    }

    private fun semesterAnzahlDecr(i: Int) = when (i) {
        1 -> q1Anzahl--
        2 -> q2Anzahl--
        3 -> q3Anzahl--
        else -> q4Anzahl--
    }


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
        scrollPane.verticalScrollBar.setUnitIncrement(16)
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
        val semesterCounts = intArrayOf(0, 0, 0, 0)
        checkboxArray.forEachIndexed { i, it -> if (it.isVisible && it.isSelected) semesterCounts[i % 4]++ }
        q1Anzahl = semesterCounts[0]
        q2Anzahl = semesterCounts[1]
        q3Anzahl = semesterCounts[2]
        q4Anzahl = semesterCounts[3]
        anzahl = semesterCounts.sum()


        // Regeln darstellen
        val regelPanel = ScrollablePanel(null)
        regelPanel.layout = BoxLayout(regelPanel, BoxLayout.PAGE_AXIS)
        regelPanel.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT)
        regelLabelArray.forEach { regelPanel.add(it) }

        // Label für die maximale Kurszahl/Semester
        regelPanel.add(kursanzahlInfoLabel)

        val scrollPane2 = JScrollPane(regelPanel)
        scrollPane2.preferredSize = Dimension(200, 350)

        checkData()

        add(scrollPane2, row = 0, column = 2, margin = Insets(0, 8, 6, 0))

        // TODO Conflicting Courses & Gruppierung der Checkboxen Implementieren
    }


    /**
     * Erstellt Checkboxen mit Labels und versteckt jene, die der Schüler nicht wählen kann
     */
    private fun buildCheckboxes() {
        // fremdsprachen & wpfs holen
        val fs = wahlData.fremdsprachen.map { it.first }

        // Zeile mit den Zählern für Semesterkurse
        checkboxPanel.add(
            JLabel("Kurse/Semester${"*".wrapHtml("a", "color:#F92F60")}".wrapHtml()).apply {
                toolTipText = "Lila hinterlegte Fächer zählen extra"
            },
            anchor = GridBagConstraints.WEST,
            margin = Insets(0, 4, 0, 0)
        )
        checkboxPanel.add(q1AnzahlLabel, row = 0, column = 1)
        checkboxPanel.add(q2AnzahlLabel, row = 0, column = 2)
        checkboxPanel.add(q3AnzahlLabel, row = 0, column = 3)
        checkboxPanel.add(q4AnzahlLabel, row = 0, column = 4)

        var af = Int.MIN_VALUE
        var offset = 1

        for ((i, fach) in fachData.faecher.withIndex()) {
            /* VO-GO Berlin - § 20 Kurse und Kursfolgen, Nr.2
                Daraus geht hervor, dass SuS jegliche Fächer zum Grundkurs wählen können,
                mit der Ausnahme von Fremdsprachen, die müssen mind. in JG 10/E-Phase begonnen
                worden sein. */
            val waehlbar: Boolean = fach.isKurs && (!fach.isFremdsprache || fach in fs)


            if (waehlbar) {
                fach.aufgabenfeld.let {
                    if (it != af) {
                        checkboxPanel.add(
                            JLabel(
                                when (it) {
                                    0 -> "Weitere"
                                    -1 -> "Zusatzkurse"
                                    else -> "Aufgabenfeld $it"
                                }
                            ).apply { font = font.deriveFont(Font.BOLD, 16f) },
                            row = i + offset, columnspan = 5, anchor = GridBagConstraints.WEST,
                            margin = Insets(6, 0, 0, 0)
                        )

                        af = it
                        offset++
                    }
                }

                val label = JLabel(fach.name)
                label.isOpaque = false

                // Wenn man das Label anklickt wird die ganze Zeile ausgewählt
                label.addMouseListener(onClick = {
                    label.foreground = Color.BLACK
                    // Checkboxen der Zeile holen
                    val zeile = checkboxArray.subList(i * 4, i * 4 + 4)

                    // An- und Abwählen
                    when (zeile.count { !it.isEnabled || it.isSelected }) {
                        0, 2, 3 -> {
                            for ((j, box) in zeile.withIndex())
                                if (box.isEnabled && !box.isSelected) {
                                    box.isSelected = true
                                    anzahl++
                                    semesterAnzahlIncr(j + 1)
                                }
                        }

                        else /* 1, 4 */ -> {
                            for ((j, box) in zeile.withIndex())
                                if (box.isEnabled && box.isSelected) {
                                    box.isSelected = false
                                    anzahl--
                                    semesterAnzahlDecr(j + 1)
                                }
                        }
                    }
                })
                labelArray[i] = label
                checkboxPanel.add(
                    label,
                    row = i + offset,
                    column = 0,
                    fill = GridBagConstraints.HORIZONTAL,
                    margin = Insets(0, 4, 0, 0)
                )
            }

            // Checkbox bauen und hinzufügen
            for (j in 1..4) {
                val box = JCheckBox()

                if (waehlbar) {
                    box.isOpaque = false
                    box.addActionListener { _ ->
                        if (box.isSelected) {
                            anzahl++
                            semesterAnzahlIncr(j)
                        } else {
                            anzahl--
                            semesterAnzahlDecr(j)
                        }
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
                checkboxPanel.add(box, row = i + offset, column = j, fill = GridBagConstraints.HORIZONTAL)
            }

            if (waehlbar && fach.isExtra) {
                checkboxPanel.add(
                    SolidFiller(extraFachColor),
                    row = i + offset,
                    column = 0,
                    columnspan = 5,
                    fill = GridBagConstraints.BOTH
                )
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

        // Blockt Fremdsprachen die in Klasse 9+ begonnen wurden
        for ((sprache, jahr) in wahlData.fremdsprachen) {
            if (jahr >= 9) {
                val pos = fachPos(sprache)
                // TODO bei Klasse 10 Belegungsverpflichtung für Künstlerisches Fach entfernen
                for (k in pos*4..pos*4 + (if (fachData.schultyp.jahre - 2 == jahr) 3 else 1)) {
                    checkboxArray[k].let {
                        it.isSelected = true
                        it.isEnabled = false
                    }
                }
            }
        }
    }

    private fun fachPos(fach: Fach) = fachData.faecher.indexOf(fach)

    override val windowName: String
        get() = "Grundkurse"
}

