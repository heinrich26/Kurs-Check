package gui

import add
import data.Fach
import data.FachData
import data.KurswahlData
import data.Wahlmoeglichkeit
import data.Wahlmoeglichkeit.*
import testFachdata
import testKurswahl
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.lang.model.element.Element
import javax.swing.*


class GrundkursWahl(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {
    override fun close(): KurswahlData {
        val gks = mutableMapOf<Fach, Wahlmoeglichkeit>()
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (fach in wahlData.pfs) continue
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
        // TODO nicht final
        return true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { GrundkursWahl(testKurswahl, testFachdata) }
        }
    }

    private val checkboxArray = ArrayList<JToggleButton>()

    private var anzahl: Int = 0
        set(value) {
            field = value
            anzahlLabel.text = "$anzahl Kurse"

            checkText.text = when {
                value < fachData.minKurse -> "Bitte wählt mindestens ${fachData.minKurse} aus"
                value > fachData.maxKurse -> "Bitte wählt maximal ${fachData.maxKurse} aus"
                else -> "Es wurden genug Kurse gewählt"
            }
        }
    private val checkText = JLabel()

    private val anzahlLabel = JLabel("$anzahl Kurse")
    val panel = JPanel()
    var ank = false

    init {
        layout = GridBagLayout()
        panel.layout = GridBagLayout()
        add(anzahlLabel, row = fachData.faecher.size)
        add(checkText, row = fachData.faecher.size + 1)

        buildCheckboxes()

        val scrollPane =
            JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.preferredSize = Dimension(250, 350)
        add(scrollPane)

        blockFae()

        //Automatisches Ankreuzen nach Tab wechsel
        //TODO das benutzen bei Pflichtfächern
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
                    anzahl++
                    checkboxArray[k + (pos * 4)].isSelected = true
                }
            }
        }

    }

    // ignorierte Fächer werden ausgeblendet das man sie nicht sieht aber das sie in dem Array sind (damit auslesen noch funktioniert)
    private fun buildCheckboxes() {
        // Erstellt Checkboxen
        for ((i, fach) in fachData.faecher.withIndex()) {
            val labs = JLabel(fach.name)

            //Wenn man ein Label anklickt werden alle Checkboxen ausgewählt
            labs.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    var cou = 0
                    val subl = checkboxArray.subList(i * 4, i * 4 + 4) //Erstellt Sublist
                    for (l in 0..3) {
                        if (subl[l].isSelected) {
                            cou++
                        }
                    }
                    //Auswählen und Abwählen
                    when (cou) {
                        in 2..3 -> {
                            for (k in i * 4..i * 4 + 3) {
                                var che = checkboxArray[k]
                                if (che.isEnabled)
                                    che.isSelected = true
                            }
                        }
                        4 -> {
                            for (k in i * 4..i * 4 + 3) {
                                var che = checkboxArray[k]
                                if (che.isEnabled)
                                    che.isSelected = false
                            }
                        }
                        0 -> {
                            for (k in i * 4..i * 4 + 3) {
                                var che = checkboxArray[k]
                                if (che.isEnabled)
                                    che.isSelected = true
                            }
                        }
                        else -> {
                            for (k in i * 4..i * 4 + 3) {
                                var che = checkboxArray[k]
                                if (che.isEnabled)
                                    che.isSelected = false
                            }
                        }
                    }
                }
            })

            panel.add(labs, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL)
            for (j in 1..4) {
                val box = JCheckBox()
                box.isOpaque = false
                box.addActionListener { if ((it.source as JToggleButton).isSelected) anzahl++ else anzahl-- }
                checkboxArray.add(box)
                panel.add(box, row = i, column = j, fill = GridBagConstraints.HORIZONTAL)
            }
        }
    }

    //TODO Fächer wie LKs und Pks müssen ausgewählt und geblockt  werden
    private fun blockFae() {
        //Blockt Prüfungsfächer
        for (pf in wahlData.pfs.filterNotNull()) {
            val pos = fachPos(pf)
            for (k in pos * 4..pos * 4 + 3) {
                anzahl++
                checkboxArray[k].let {
                    it.isSelected = true
                    it.isEnabled = false
                }
            }
        }
        //Blockt Pflichtfächer
        //TODO Jetzt Alle gesperrt, aber es gibt auch fächer wo nur bestimmte gesperrt
        for ((pf, wm) in fachData.pflichtfaecher) {
            val pos = fachPos(pf)
            for (k in pos * 4..pos * 4 + 3) {
                anzahl++
                checkboxArray[k].let {
                    it.isSelected = true
                    it.isEnabled = false
                }
            }
        }
    }

    private fun fachPos(fach: Fach) = fachData.faecher.indexOf(fach)

    override val windowName: String
        get() = "Grundkurse"
}

