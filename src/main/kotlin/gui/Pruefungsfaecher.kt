package gui

import ExclusiveComboBoxModel
import add
import data.*
import testFachdata
import testKurswahl
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.*
import javax.swing.*


class Pruefungsfaecher(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Pruefungsfaecher(testKurswahl, testFachdata) }
        }
    }

    private val pf3: FachComboBox
    private val pf4: FachComboBox
    private val pf5: FachComboBox


    init {
        this.layout = GridBagLayout()

        add(
            JLabel("Prüfungsfächer:"),
            column = 0,
            columnspan = 2,
            margin = Insets(0, 0, 4, 0),
            anchor = GridBagConstraints.WEST
        )
        add(Box.createHorizontalStrut(50), column = 2)

        val fs = wahlData.fremdsprachen.map { it.first }
        val wpfs = wahlData.wpfs
        val prefilteredZeilen = fachData.filterWahlzeilen(wahlData.lk1, wahlData.lk2)
        println(prefilteredZeilen)
        val pf3faecher = faecherAusWahlzeilen(prefilteredZeilen, fs, wpfs)

        // geht schon
        val model1 = FachComboBoxModel(pf3faecher)
        pf3 = FachComboBox(model1)

        // wip
        val model2 = ExclusiveComboBoxModel(fachData.faecher, pf3)
        pf4 = FachComboBox(model2)

        val model3 = ExclusiveComboBoxModel(fachData.faecher, pf4)
        pf5 = FachComboBox(model3)


        pf3.renderer = FachRenderer
        pf4.renderer = FachRenderer
        pf5.renderer = FachRenderer


        // Daten einsetzen
        pf3.selectedItem = wahlData.pf3
        pf4.selectedItem = wahlData.pf4
        pf5.selectedItem = wahlData.pf5


        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(pf3, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf4, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf5, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

        // Beschriftungen hinzufügen
        for (i in 3..5) {
            add(JLabel("PF $i."), row = i - 2, column = 0)
        }
    }


    /**
     * Gibt die Fächer, die die gegebenen Wahlzeilen zulassen zurück
     * TODO funzt noch nicht so ganz
     */
    private fun faecherAusWahlzeilen(
        wahlzeilen: Map<Int, Wahlzeile>,
        fremdsprachen: List<Fach>,
        wpfs: Pair<Fach, Fach?>?
    ): List<Fach> =
        LinkedHashSet(wahlzeilen.values.flatMap { wz ->
            val kuerzel = wz.pf3
            if (kuerzel == "*") fachData.faecher else // TODO Pfrüfungsfächer Liste erstellen
            if (kuerzel.startsWith("$")) fachData.wzWildcards[kuerzel]!!
            else Collections.singleton(fachData.faecherMap[kuerzel]!!)
        }).filter {
            if (it.fremdsprache) it in fremdsprachen else
                    /* Hat keine WPF or Fach ist weder 1./2. WPF */
                    (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second)))
        }

    override fun close(): KurswahlData =
        wahlData.updatePFs(pf3 = pf3.selectedItem!!, pf4 = pf4.selectedItem!!, pf5 = pf5.selectedItem!!)

    override fun isDataValid(): Boolean =
        (pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null)

    override val windowName: String
        get() = "Prüfungsfächer"
}