package gui

import ChainedSpinnerNumberModel
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
import java.awt.event.ActionEvent
import javax.swing.*
import kotlin.math.max


class Leistungskurse(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Leistungskurse(testKurswahl, testFachdata) }
        }

        class MyComboBox(model: ExclusiveComboBoxModel) : JComboBox<Fach>(model) {
            override fun getSelectedIndex(): Int = max(super.getSelectedIndex(), 0)

            override fun getSelectedItem(): Fach? {
                return super.getSelectedItem() as Fach?
            }
        }
    }

    private val lk1: MyComboBox
    private val lk2: MyComboBox

    init {
        this.layout = GridBagLayout()

        add(
            JLabel("Leistungskurse:"),
            column = 0,
            columnspan = 2,
            margin = Insets(0, 0, 4, 0),
            anchor = GridBagConstraints.WEST
        )
        add(Box.createHorizontalStrut(50), column = 2)
        


        val model1 = ExclusiveComboBoxModel(fachData.fremdsprachen)
        lk1 = MyComboBox(model1)


        val model2 = ExclusiveComboBoxModel(fachData.fremdsprachen, lk1)
        lk2 = MyComboBox(model2)



        
        val renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component? = super.getListCellRendererComponent(
                list,
                if (value is Fach) value.name else "Ungesetzt",
                index,
                isSelected,
                cellHasFocus
            )
        }

        lk1.renderer = renderer
        lk2.renderer = renderer


        // Daten einsetzen
        lk1.selectedItem = wahlData.lk1
        lk2.selectedItem = wahlData.lk2

        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(lk1, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(lk2, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }
    }


    override fun close(): KurswahlData {


        return wahlData.copy(lk1 = lk1.selectedItem, lk2 = lk2.selectedItem)
    }

    override fun isDataValid(): Boolean {
        return (lk1.selectedItem != null && lk2.selectedItem != null)
    }

    override val windowName: String
        get() = "Fremdsprachen & Wahlpflichtfächer"
}