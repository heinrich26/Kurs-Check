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

import com.kurswahlApp.data.*
import com.kurswahlApp.data.Consts.COLOR_CONTROL
import com.kurswahlApp.data.Wahlmoeglichkeit.*
import com.kurswahlApp.data.Wahlmoeglichkeit.Companion.UNGEWAEHLT_BOOLS
import org.intellij.lang.annotations.Language
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*


class GrundkursWahl(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData = wahlData.copy(gks = buildMap {
        val pfs = wahlData.pfs
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (fach in pfs) continue
            // Speichern der gewählten Grundkurse und dessen Semester
            put(fach, checkboxRows[i]?.toWahlmoeglichkeit() ?: continue)
        }
    })

    override fun isDataValid(): Boolean {
        if (invalidRows != 0) return false

        // Überprüfen, dass pro Semester die maximale Kurszahl nicht überschritten wird
        if (!close().countCourses(true).zip(fachData.semesterkurse).all { it.first <= it.second }) return false

        // Überprüfen, dass die maximale Kurszahl nicht überschritten wird
        return checkboxRows.sumOf { it?.count() ?: 0 } in fachData.minKurse..fachData.maxKurse
    }

    @Language("HTML")
    override fun showHelp(): String {
        val toc = StringBuilder("<ul>")
        val body = StringBuilder()
        var af: Int = Int.MIN_VALUE
        for (fach in fachData.faecher) {
            if (!fach.isKurs || fach.infoText == null) continue

            if (af != fach.aufgabenfeld) {
                // Im ersten Durchlauf nicht die Listen beenden.
                if (af != Int.MIN_VALUE) toc.append("</ul></li>")

                af = fach.aufgabenfeld
                when (af) {
                    0 -> "Weitere"
                    -1 -> "Zusatzkurse"
                    else -> "Aufgabenfeld $af"
                }.let {
                    body.append("<h2><a name='af$af'>$it</a></h2>")
                    toc.append("<li><a href='#af$af'>$it</a><ul>")
                }

            }
            toc.append("<li><a href='#fach-${fach.kuerzel}'>${fach.name}</a></li>")
            body.append("<h3><a name='fach-${fach.kuerzel}'>${fach.name}</a></h3><p>${fach.infoText}</p>")
        }
        toc.append("</ul></li></ul>")

        if (body.isEmpty()) {
            body.append("<p>Niemand hat einen Infotext für deine Fächer geschrieben... :(</p><p>Beschwer dich bei deinem/deiner PäKo, damit sich das ändert!</p>")
            toc.clear()
        }

        // language=html
        return "<h1>$windowName</h1>\n<ol>\n    <li><a href='#eingabehilfe'>Eingabe-Hilfe</a></li>\n    <li><a href='#faecher'>Fächer</a></li>\n</ol>\n\n<h2><a name='eingabehilfe'>Eingabe-Hilfe</a></h2>\n<p>Auf der linken Seite kannst du Kurse auswählen. Kurse die du bereits als Prüfungsfächer hast, sind\n    bereits angeklickt! Auf der rechten Seite findest du alle Regeln, die deine Grundkurse erfüllen müssen! Wähle ein\n    paar Kurse und klicke auf <b>Überprüfen</b> um zu sehen welche Fächer/Kurse dir noch fehlen!</p>\n<p>Die Grundkurse ergeben sich wie folgt:<br><b>Ich bin leider nicht lyrisch begabt, deswegen beschwere dich bitte bei\n    deinem PäKo, dass er/sie keine bessere Hilfe verfasst hat!</b></p>\n<h2><a name='faecher'>Fächer</a></h2>$toc$body"
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { GrundkursWahl(testKurswahl, testFachdata) }
        }

        private val extraFachColor = COLOR_CONTROL

        private val LABEL_TEXT_COLOR = JLabel().foreground
    }

    private var dataValid = true
    private var invalidRows = 0
        set(value) {
            if (field != 0 && value == 0) {
                if (dataValid)
                    notifier.invoke(true)
            } else if (field == 0 && value != 0) {
                notifier.invoke(false)
            }

            field = value
        }

    // Repräsentiert eine Zeile von Checkboxen für ein Fach
    private inner class CheckboxRow(fach: Fach) : JPanel(GridBagLayout()) {
        private val label = JLabel(fach.name)

        private val zeile = arrayOf(JCheckBox(), JCheckBox(), JCheckBox(), JCheckBox())

        val isExtra = fach.isExtra

        var isRowValid = true

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

            label.addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent?) {
                    if (label.size.width < label.preferredSize.width) {
                        label.toolTipText = label.text
                    }
                }
            })

            add(
                label,
                row = 0,
                column = 0,
                fill = GridBagConstraints.BOTH,
                weightx = 1.0,
                margin = Insets(left = 4)
            )

            // TODO Info Button machen
            // vorher die nachfolgenden Checkboxen und den Extra-Backdrop eine Zelle nach rechts verschieben
//            val infoButton = HoverableIconButton(R.info_outline, 10) { println("hallo") }
//            add(infoButton, row = 0, column = 1)

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
                    if (zeile.map(JCheckBox::isSelected).let {
                            it == UNGEWAEHLT_BOOLS || Wahlmoeglichkeit.fromBools(it) != null
                        }) {
                        if (!isRowValid) {
                            isRowValid = true
                            invalidRows--
                            label.foreground = Color.BLACK
                        }
                    } else if (isRowValid) {
                        isRowValid = false
                        invalidRows++
                        label.foreground = Consts.COLOR_ERROR
                    }
                }

                add(
                    box, row = 0, column = i + 2,
                    anchor = GridBagConstraints.EAST,
                    fill = GridBagConstraints.HORIZONTAL
                )
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
            fach.nurIn.bools.forEachIndexed { i, b -> if (!b) zeile[i].isEnabled = false }
        }

        fun apply(wm: Wahlmoeglichkeit, block: Boolean = false) {
            val f: (Pair<JCheckBox, Boolean>) -> Unit = if (block) { (box, state) ->
                box.isSelected = state
                box.isEnabled = !state
            } else { (box, state) ->
                box.isSelected = state
            }
            zeile.zip(wm.bools).forEach(f)
        }

        fun selection() = zeile.map(JCheckBox::isSelected)

        fun toWahlmoeglichkeit(): Wahlmoeglichkeit? = Wahlmoeglichkeit.fromBools(selection())

        fun count(): Int = toWahlmoeglichkeit()?.n ?: 0
    }

    private fun checkData(): Boolean {
        val data = close()
        data.lock()
        // Muss komplett evaluiert werden! (List.all() wäre Lazy -> würde nicht alle Regeln updaten)
        return regelLabelArray.map { it.match(data) }.all { it } and kursanzahlInfoLabel.match(semesterkurse)
    }


    private val checkboxRows = arrayOfNulls<CheckboxRow>(fachData.faecher.size)

    private val regelLabelArray = fachData.regeln.map(::RegelLabel).toTypedArray()

    // Absolutes Minimum/Maximum an Kursen wählbar! Zählt auch Extrakurse
    private var anzahl: Int = 0
        set(value) {
            field = value
            anzahlLabel.text = "$anzahl Kurse"

            when {
                value < fachData.minKurse -> {
                    anzahlInfoLabel.text = "Bitte wähle mindestens ${fachData.minKurse} Kurse"
                    anzahlInfoLabel.foreground = Consts.COLOR_ERROR
                }

                value > fachData.maxKurse -> {
                    anzahlInfoLabel.text = "Bitte wähle maximal ${fachData.maxKurse} Kurse"
                    anzahlInfoLabel.foreground = Consts.COLOR_ERROR

                }

                else -> {
                    anzahlInfoLabel.text = "Es wurden genug Kurse gewählt"
                    anzahlInfoLabel.foreground = LABEL_TEXT_COLOR
                }
            }
        }

    private val anzahlLabel = JLabel("$anzahl Kurse")
    private val anzahlInfoLabel = JLabel("")
    private val kursanzahlInfoLabel = KursanzahlInfo(fachData.semesterkurse)

    // Labels für die Kurse/Semester
    private val q1AnzahlLabel = JLabel("0", JLabel.CENTER)
    private val q2AnzahlLabel = JLabel("0", JLabel.CENTER)
    private val q3AnzahlLabel = JLabel("0", JLabel.CENTER)
    private val q4AnzahlLabel = JLabel("0", JLabel.CENTER)
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


    private val checkboxPanel = ScrollablePanel(GridBagLayout()).apply { setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT) }

    private val checkButton = JButton("Überprüfen")

    init {
        add(anzahlLabel, row = 1)
        add(anzahlInfoLabel, row = 1, column = 0, columnspan = 3)

        checkButton.addActionListener { notifier.invoke((invalidRows == 0) and checkData()) }
        add(checkButton, row = 1, column = 2, anchor = GridBagConstraints.EAST)

        buildCheckboxes()

        val scrollPane =
            JScrollPane(
                checkboxPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            )
        scrollPane.preferredSize = Dimension(275, 350)
        scrollPane.verticalScrollBar.unitIncrement = 16
        add(scrollPane, row = 0, column = 0, columnspan = 2, margin = Insets(bottom = 6))

        faecherBlocken()

        // Grundkurse (Daten) eintragen
        for ((gk, choice) in wahlData.gks) {
            checkboxRows[fachPos(gk)]?.apply(choice)
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
        regelLabelArray.forEach(regelPanel::add)

        // Label für die maximale Kurszahl/Semester
        regelPanel.add(kursanzahlInfoLabel)

        val scrollPane2 = JScrollPane(regelPanel)
        scrollPane2.preferredSize = Dimension(200, 350)

        add(scrollPane2, row = 0, column = 2, margin = Insets(left = 8, bottom = 6))

        notifier.invoke(checkData())
    }


    /**
     * Erstellt Checkboxen mit Labels und versteckt jene, die der Schüler nicht wählen kann
     */
    private fun buildCheckboxes() {
        // fremdsprachen & wpfs holen
        val fs = wahlData.fremdsprachen.map(Pair<Fach, *>::first)

        // Zeile mit den Zählern für Semesterkurse
        with(JPanel(GridBagLayout())) {
            add(
                JLabel("Kurse/Semester${"*".wrapHtml("a", "color:#F92F60")}".wrapHtml()).apply {
                    toolTipText = "Lila hinterlegte Fächer zählen extra"
                },
                anchor = GridBagConstraints.WEST,
                weightx = 1.0,
                fill = GridBagConstraints.HORIZONTAL,
                margin = Insets(left = 4)
            )

            Dimension(JCheckBox().preferredSize.width, q1AnzahlLabel.preferredSize.height).let {
                q1AnzahlLabel.preferredSize = it
                q2AnzahlLabel.preferredSize = it
                q3AnzahlLabel.preferredSize = it
                q4AnzahlLabel.preferredSize = it
            }

            add(q1AnzahlLabel, row = 0, column = 1)
            add(q2AnzahlLabel, row = 0, column = 2)
            add(q3AnzahlLabel, row = 0, column = 3)
            add(q4AnzahlLabel, row = 0, column = 4)

            checkboxPanel.add(this, fill = GridBagConstraints.HORIZONTAL, weightx = 1.0)
        }
        var af = Int.MIN_VALUE
        var offset = 1

        for ((i, fach) in fachData.faecher.withIndex()) {
            /* VO-GO Berlin - § 20 Kurse und Kursfolgen, Nr.2
                Daraus geht hervor, dass SuS jegliche Fächer zum Grundkurs wählen können,
                mit der Ausnahme von Fremdsprachen, welche mind. in JG 10/E-Phase begonnen
                worden sein müssen. (schlussfolgerung, da Satz 1 nicht definiert, wvl. Wochenstunden eine
                neue Fremdsprache in Sek II hätte) */
            if (!fach.isKurs
                || fach.isFremdsprache && fach !in fs
                || !fach.checkKlasse(wahlData.klasse)
                || fach.nurLk && fach !in wahlData.lks
            ) continue

            // Unterteilung anhand des Aufgabenfelds vornehmen, neue Überschriften hinzufügen
            if (fach.aufgabenfeld != af) {
                af = fach.aufgabenfeld
                checkboxPanel.add(
                    JLabel(
                        when (af) {
                            0 -> "Weitere"
                            -1 -> "Zusatzkurse"
                            else -> "Aufgabenfeld $af"
                        }
                    ).apply { font = font.deriveFont(Font.BOLD, 16f) },
                    row = i + offset, anchor = GridBagConstraints.WEST,
                    margin = Insets(top = 6, left = 4)
                )
                offset++
            }

            val row = CheckboxRow(fach)
            checkboxRows[i] = row
            checkboxPanel.add(
                row,
                row = i + offset,
                column = 0,
                columnspan = 5,
                fill = GridBagConstraints.HORIZONTAL,
                weightx = 1.0
            )
        }
    }

    /**
     * Blockt Prüfungs- und Pflichtfächer, damit der Nutzer nichts an der auswahl verändern kann
     */
    private fun faecherBlocken() {
        // Blockt Prüfungsfächer
        for (pos in wahlData.pfs.mapNotNull { it?.let { fachPos(it) } }) {
            checkboxRows[pos]!!.apply(DURCHGEHEND, true)
        }
        // Blockt Pflichtfächer
        for ((pf, wm) in fachData.pflichtfaecher) {
            checkboxRows[fachPos(pf)]!!.apply(wm, true)
        }

        // Blockt Fremdsprachen die in Klasse 9+ begonnen wurden
        for ((sprache, jahr) in wahlData.fremdsprachen) {
            if (jahr >= 9) {
                // TODO bei Klasse 10 Belegungsverpflichtung für künstlerisches Fach entfernen
                checkboxRows[fachPos(sprache)]!!.apply(
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

