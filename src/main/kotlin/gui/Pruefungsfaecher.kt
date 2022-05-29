package gui

import ExclusiveComboBoxModel
import add
import data.Fach
import data.FachData
import data.KurswahlData
import testFachdata
import testKurswahl
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
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


        val model1 = ExclusiveComboBoxModel(fachData.faecher)
        pf3 = FachComboBox(model1)

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


    override fun close(): KurswahlData =
        wahlData.updatePFs(pf3 = pf3.selectedItem!!, pf4 = pf4.selectedItem!!, pf5 = pf5.selectedItem!!)

    override fun isDataValid(): Boolean =
        (pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null)

    override val windowName: String
        get() = "Prüfungsfächer"
}