package com.kurswahlApp.gui

import com.kurswahlApp.add
import com.kurswahlApp.data.*
import com.kurswahlApp.data.Wahlmoeglichkeit.*
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*


class GrundkursWahl(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData {
        val gks = mutableMapOf<Fach, Wahlmoeglichkeit>()
        val pfs = wahlData.pfs
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (fach in pfs) continue
            //Übergabe der gewählten Grundkurse und dessen Semester
            val value = when (checkboxArray.subList(i * 4, i * 4 + 4).map { it.isSelected }) {
                listOf(true, true, false, false) -> ERSTES_ZWEITES
                listOf(true, true, true, false) -> ERSTES_DRITTES
                listOf(false, true, true, true) -> ZWEITES_VIERTES
                listOf(false, false, true, true) -> DRITTES_VIERTES
                listOf(true, true, true, true) -> DURCHGEHEND
                else -> continue
            }
            gks[fach] = value
        }

        return wahlData.copy(gks = gks.toMap())
    }

    override fun isDataValid(): Boolean {
        val pfs = wahlData.pfs
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (fach in pfs) continue
            // Übergabe der gewählten Grundkurse und dessen Semester
            when (checkboxArray.subList(i * 4, i * 4 + 4).map { it.isSelected }) {
                listOf(true, true, false, false), listOf(true, true, true, false),
                listOf(false, true, true, true), listOf(false, false, true, true),
                listOf(true, true, true, true), listOf(false, false, false, false) -> continue
                else -> return false
            }
        }
        return true
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

    private val checkboxArray = ArrayList<JToggleButton>()

    private val regelLabelArray = fachData.regeln.map { RegelLabel(it) }.toTypedArray()

    private var anzahl: Int = 0
        set(value) {
            field = value
            anzahlLabel.text = "$anzahl Kurse"

            anzahlInfoLabel.text = when {
                value < fachData.minKurse -> "Bitte wähle mindestens ${fachData.minKurse} Kurse"
                value > fachData.maxKurse -> "Bitte wähle maximal ${fachData.maxKurse} Kurse"
                else -> "Es wurden genug Kurse gewählt"
            }
        }

    private val anzahlLabel = JLabel("$anzahl Kurse")
    private val anzahlInfoLabel = JLabel()

    private val panel = JPanel(GridBagLayout())

    private val checkButton = JButton("Überprüfen")

    init {
        add(anzahlLabel, row = 1)
        add(anzahlInfoLabel, row = 1)
        checkButton.addActionListener { checkData() }
        add(checkButton, row = 1)

        buildCheckboxes()

        val scrollPane =
            JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.preferredSize = Dimension(250, 350)
        add(scrollPane)

        faecherBlocken()

        // Grundkurse (Daten) eintragen
        for ((gk, choice) in wahlData.gks) {
            val pos = fachPos(gk)
            val acti = when (choice) {
                ERSTES_ZWEITES -> listOf(true, true, false, false)
                ERSTES_DRITTES -> listOf(true, true, true, false)
                ZWEITES_VIERTES -> listOf(false, true, true, true)
                DRITTES_VIERTES -> listOf(false, false, true, true)
                DURCHGEHEND -> listOf(true, true, true, true)
            }
            for (k in 0..3) {
                if (acti[k]) {
                    checkboxArray[k + (pos * 4)].isSelected = true
                }
            }
        }


        // Kurse zählen
        /*for (box in checkboxArray) {
            if (box.isVisible && box.isSelected) anzahl++
        }
        Equivalent zu: */
        anzahl = checkboxArray.count { it.isVisible && it.isSelected }


        // Regeln darstellen
        val regelPanel = ScrollablePanel(null)
        regelPanel.layout = BoxLayout(regelPanel, BoxLayout.PAGE_AXIS)
        regelPanel.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT)
        regelLabelArray.forEach { regelPanel.add(it) }

        val scrollPane2 = JScrollPane(regelPanel)
        scrollPane2.preferredSize = Dimension(200, 350)

        checkData()

        add(scrollPane2)
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
                if (fach.fremdsprache) fach in fs
                else (!fach.brauchtWPF || (wpfs != null && (fach == wpfs.first || fach == wpfs.second)))
            // cond == true -> wählbar, sonst versteckt


            if (cond) {
                val label = JLabel(fach.name)

                // Wenn man das Label anklickt wird die ganze Zeile ausgewählt
                label.addMouseListener(object : MouseAdapter() {
                    override fun mousePressed(e: MouseEvent) {
                        // Checkboxen der Zeile holen
                        val zeile = checkboxArray.subList(i * 4, i * 4 + 4)

                        // An- und Abwählen
                        when (zeile.count { it.isSelected }) {
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
                })
                panel.add(label, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL)
            }

            // Checkbox bauen und hinzufügen
            for (j in 1..4) {
                val box = JCheckBox()

                if (cond) {
                    box.isOpaque = false
                    box.addActionListener { if ((it.source as JToggleButton).isSelected) anzahl++ else anzahl-- }
                } else box.isVisible = false

                checkboxArray.add(box)
                panel.add(box, row = i, column = j, fill = GridBagConstraints.HORIZONTAL)
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
        // TODO zurzeit sind 1-4 gesperrt, aber es könnte auch Fächer geben, wo nur bestimmte gesperrt sind
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

