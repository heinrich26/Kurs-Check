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
import com.kurswahlApp.data.Wahlmoeglichkeit.Companion.UNGEWAEHLT_BOOLS
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
            gks[fach] = checkboxRows[i]?.toWahlmoeglichkeit() ?: continue
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
            val row = checkboxRows[i] ?: continue
            // Übergabe der gewählten Grundkurse und dessen Semester
            counter += row.selection().count { it }
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

    // Repräsentiert eine Zeile von Checkboxen für ein Fach
    inner class CheckboxRow(fach: Fach) : JPanel(GridBagLayout()) {
        private val label = JLabel(fach.name).also { isOpaque = false }

        private val zeile = arrayOf(JCheckBox(), JCheckBox(), JCheckBox(), JCheckBox())

        val isExtra = fach.isExtra

        init {
            label.addMouseListener(onClick = {
                // War die Zeile vorher invalid, wird sie jetzt auf jeden Fall valid!
                label.foreground = Color.BLACK

                // An- und Abwählen
                when (zeile.count { !it.isEnabled || it.isSelected }) {
                    0, 2, 3 -> {
                        for ((i, box) in zeile.withIndex())
                            if (box.isEnabled && !box.isSelected) {
                                box.isSelected = true
                                anzahl++

                                // Nur bei zählenden Kursen zusammenrechnen, keine Extra-Kurse
                                if (!isExtra) semesterAnzahlIncr(i)
                            }
                    }

                    else /* 1, 4 */ -> {
                        for ((i, box) in zeile.withIndex())
                            if (box.isEnabled && box.isSelected) {
                                box.isSelected = false
                                anzahl--

                                // Nur bei zählenden Kursen zusammenrechnen, keine Extra-Kurse
                                if (!isExtra) semesterAnzahlDecr(i)
                            }
                    }
                }
            })

            add(
                label,
                row = 0,
                column = 0,
                fill = GridBagConstraints.BOTH,
                weightx = 1.0,
                margin = Insets(0, 4, 0, 0)
            )

            for ((i, box) in zeile.withIndex()) {
                box.isOpaque = false

                box.addActionListener { _ ->
                    if (box.isSelected) {
                        anzahl++
                        if (!isExtra) semesterAnzahlIncr(i)
                    } else {
                        anzahl--
                        if (!isExtra) semesterAnzahlDecr(i)
                    }
                    // Das Label Rot färben, wenn die Reihe ungültig ist
                    label.foreground =
                        if (zeile.map { it.isSelected }.let {
                                it == UNGEWAEHLT_BOOLS || Wahlmoeglichkeit.fromBools(it) != null
                            }) Color.BLACK
                        else Consts.COLOR_ERROR
                }

                add(box, row = 0, column = i + 1,
                    anchor = GridBagConstraints.EAST)
            }

            if (isExtra)
                add(
                    SolidFiller(extraFachColor),
                    row = 0,
                    column = 0,
                    columnspan = 5,
                    fill = GridBagConstraints.BOTH
                )

            // Blockt Checkboxen für Fächer die nur in bestimmten Semestern gewählt werden können
            when (fach.nurIn) {
                ERSTES_ZWEITES -> {
                    zeile[2].isEnabled = false
                    zeile[3].isEnabled = false
                }

                DRITTES_VIERTES -> {
                    zeile[0].isEnabled = false
                    zeile[1].isEnabled = false
                }

                ERSTES_DRITTES -> {
                    zeile[3].isEnabled = false
                }

                ZWEITES_VIERTES -> {
                    zeile[0].isEnabled = false
                }

                else -> {}
            }
        }

        fun apply(wm: Wahlmoeglichkeit, block: Boolean = false) {
            val f: (Pair<JCheckBox, Boolean>) -> Unit = if (block) { (box, state) ->
                box.isSelected = state
                box.isEnabled = !state
            }
            else { (box, state) ->
                box.isSelected = state
            }
            zeile.zip(wm.bools).forEach(f)
        }

        fun selection() = zeile.map { it.isSelected }

        fun toWahlmoeglichkeit() = Wahlmoeglichkeit.fromBools(selection())
    }

    private fun checkData(): Boolean {
        val data = close()
        data.lock()
        return regelLabelArray.map {
            it.match(data)
        }.all { it } && kursanzahlInfoLabel.match(semesterkurse)
    }


    private val checkboxRows = arrayOfNulls<CheckboxRow>(fachData.faecher.size)

    private val regelLabelArray = fachData.regeln.map { RegelLabel(it) }.toTypedArray()

    // Absolutes Minimum/Maximum an Kursen wählbar! Zählt auch Extrakurse
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
        0 -> q1Anzahl++
        1 -> q2Anzahl++
        2 -> q3Anzahl++
        else -> q4Anzahl++
    }

    private fun semesterAnzahlDecr(i: Int) = when (i) {
        0 -> q1Anzahl--
        1 -> q2Anzahl--
        2 -> q3Anzahl--
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
            JScrollPane(
                checkboxPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            )
        scrollPane.preferredSize = Dimension(250, 350)
        scrollPane.verticalScrollBar.setUnitIncrement(16)
        add(scrollPane, row = 0, column = 0, columnspan = 2, margin = Insets(0, 0, 6, 0))

        faecherBlocken()

        // Grundkurse (Daten) eintragen
        for ((gk, choice) in wahlData.gks) {
            val pos = fachPos(gk)
            checkboxRows[pos]?.apply(choice)
        }


        // Kurse zählen
        var extra = 0
        val semesterCounts = intArrayOf(0, 0, 0, 0)
        checkboxRows.flatMap {
            it?.let {
                if (it.isExtra) {
                    extra += it.toWahlmoeglichkeit()?.n ?: 0
                    return@flatMap UNGEWAEHLT_BOOLS
                } else it.selection()
            } ?: UNGEWAEHLT_BOOLS
        }.forEachIndexed { i, selected -> if (selected) semesterCounts[i % 4]++ }
        q1Anzahl = semesterCounts[0]
        q2Anzahl = semesterCounts[1]
        q3Anzahl = semesterCounts[2]
        q4Anzahl = semesterCounts[3]

        anzahl = semesterCounts.sum() + extra


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
        with(JPanel(GridBagLayout())) {
            add(
                JLabel("Kurse/Semester${"*".wrapHtml("a", "color:#F92F60")}".wrapHtml()).apply {
                    toolTipText = "Lila hinterlegte Fächer zählen extra"
                },
                anchor = GridBagConstraints.WEST,
                weightx = 1.0, fill = GridBagConstraints.HORIZONTAL,
                margin = Insets(0, 4, 0, 0)
            )
            add(q1AnzahlLabel, row = 0, column = 1)
            add(q2AnzahlLabel, row = 0, column = 2)
            add(q3AnzahlLabel, row = 0, column = 3)
            add(q4AnzahlLabel, row = 0, column = 4)

            add(Box.createHorizontalStrut(21), row = 0, column = 1)
            add(Box.createHorizontalStrut(21), row = 0, column = 2)
            add(Box.createHorizontalStrut(21), row = 0, column = 3)
            add(Box.createHorizontalStrut(21), row = 0, column = 4)

            checkboxPanel.add(this, fill = GridBagConstraints.HORIZONTAL)
        }
        var af = Int.MIN_VALUE
        var offset = 1

        for ((i, fach) in fachData.faecher.withIndex()) {
            /* VO-GO Berlin - § 20 Kurse und Kursfolgen, Nr.2
                Daraus geht hervor, dass SuS jegliche Fächer zum Grundkurs wählen können,
                mit der Ausnahme von Fremdsprachen, die müssen mind. in JG 10/E-Phase begonnen
                worden sein. */
            val waehlbar: Boolean = fach.isKurs && (!fach.isFremdsprache || fach in fs) && fach.checkKlasse(wahlData.klasse)


            if (waehlbar) {
                // Unterteilung anhand des Aufgabenfelds vornehmen
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
                            row = i + offset, anchor = GridBagConstraints.WEST,
                            margin = Insets(6, 4, 0, 0)
                        )

                        af = it
                        offset++
                    }
                }

                val row = CheckboxRow(fach)
                checkboxRows[i] = row
                checkboxPanel.add(row, row = i + offset, column = 0, columnspan = 5, fill = GridBagConstraints.HORIZONTAL, weightx = 1.0)
            }
        }
    }

    /**
     * Blockt Prüfungs- und Pflichtfächer, damit der Nutzer nichts an der auswahl verändern kann
     */
    private fun faecherBlocken() {
        // Blockt Prüfungsfächer
        for (pos in wahlData.pfs.filterNotNull().map { fachPos(it) }) {
            checkboxRows[pos]!!.apply(DURCHGEHEND, true)
        }
        // Blockt Pflichtfächer
        for ((pf, wm) in fachData.pflichtfaecher) {
            val pos = fachPos(pf)
            checkboxRows[pos]!!.apply(wm, true)
        }

        // Blockt Fremdsprachen die in Klasse 9+ begonnen wurden
        for ((sprache, jahr) in wahlData.fremdsprachen) {
            if (jahr >= 9) {
                val pos = fachPos(sprache)
                // TODO bei Klasse 10 Belegungsverpflichtung für Künstlerisches Fach entfernen
                checkboxRows[pos]!!.apply(
                    if (fachData.schultyp.jahre - 2 == jahr) DURCHGEHEND else ERSTES_ZWEITES,
                    true
                )

            }
        }
    }

    private fun fachPos(fach: Fach) = fachData.faecher.indexOf(fach)

    override val windowName: String
        get() = "Grundkurse"
}

