package gui

import add
import data.Fach
import data.FachData
import data.KurswahlData
import data.Wahlmoeglichkeit
import data.Wahlmoeglichkeit.*
import testFachdata
import testKurswahl
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JToggleButton


class GrundkursWahl(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {
    override fun close(): KurswahlData {
        val gks = ArrayList<Pair<Fach, Wahlmoeglichkeit>>()
        for ((i, fach) in fachData.feacher.withIndex()) {
            if (fach in wahlData.pfs) continue

            val zeile = checkboxArray.subList(i * 4, i * 4 + 4).map { it.isSelected }
            val pair = fach to when (zeile) {
                listOf(true, true, false, false) -> ERSTES_ZWEITES
                listOf(true, true, true, false) -> ERSTES_DRITTES
                listOf(false, true, true, true) -> ZWEITES_VIERTES
                listOf(false, false, true, true) -> DRITTES_VIERTES
                listOf(true, true, true, true) -> DURCHGEHEND
                else -> continue
            }
            gks.add(pair)
        }
        return wahlData.copy(gks = gks)
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
        }
    private val anzahlLabel = JLabel("$anzahl Kurse")

    init {
        layout = GridBagLayout()
        add(anzahlLabel, row = fachData.feacher.size)

        buildCheckboxes()
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
        for ((i, fach) in fachData.feacher.withIndex()) {
            val labs = JLabel(fach.name)
            add(labs, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL)
            for (j in 1..4) {
//                val box = ColoredCheckBox()
                val box = JCheckBox()
                box.isOpaque = false
                box.addActionListener { if ((it.source as JToggleButton).isSelected) anzahl++ else anzahl-- }
                checkboxArray.add(box)
                add(box, row = i, column = j, fill = GridBagConstraints.HORIZONTAL)
            }
        }
    }

    private fun fachPos(fach: Fach) = fachData.feacher.indexOf(fach)

    override val windowName: String
        get() = "Grundkurse"

}