package gui

import add
import data.Fach
import data.FachData
import data.KurswahlData
import data.Wahlmoeglichkeit
import data.Wahlmoeglichkeit.*
import testFachdata
import testKurswahl
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


class GrundkursWahl(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {
    override fun close(): KurswahlData {
        val gks = ArrayList<Pair<Fach, Wahlmoeglichkeit>>()
        for ((i, fach) in fachData.feacher.withIndex()) {
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
        //TODO nicht final
        return true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { GrundkursWahl(testKurswahl, testFachdata) }
        }
    }


    val checkboxArray = ArrayList<JCheckBox>()
    var anzahl: Int = 0
        set(value) {
            field = value
            anzahlLabel.text = "$anzahl Kurse"
        }
    val anzahlLabel = JLabel("$anzahl Kurse")

    init {
        layout = GridBagLayout()
        add(anzahlLabel, row = fachData.feacher.size)

        val blackline = BorderFactory.createLineBorder(Color(229, 229, 229), 2)
        setBounds(150, 250, 1500, 700)
        background = Color.white
        border = blackline

        checkboxenUp()
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
                if (acti[k])
                    anzahl++
                checkboxArray[k + pos * 4].let {
                    it.isSelected = true
                }
            }
        }

    }

    //ignorierte Fächer werden ausgebländet das man sie nicht sieht aber das sie in dem Array sind

    fun checkboxenUp() {
        //Erstellt Checkboxen
        for ((i, fach) in fachData.feacher.withIndex()) {
            val labs = JLabel(fach.name)
            add(labs, row = i, column = 0, fill = GridBagConstraints.HORIZONTAL)
            for (j in 1..4) {
                val n = JCheckBox()
                n.setFocusable(false)
                n.setOpaque(false)
                n.addActionListener { if ((it.source as JCheckBox).isSelected) anzahl++ else anzahl--}
                n.background = Color.CYAN
                checkboxArray.add(n)
                add(n, row = i, column = j, fill = GridBagConstraints.HORIZONTAL)
                //Locked Fächer
            }
        }
    }

    fun fachPos(fach: Fach) = fachData.feacher.indexOf(fach)

    override val windowName: String
        get() = "Grundkurse"

}