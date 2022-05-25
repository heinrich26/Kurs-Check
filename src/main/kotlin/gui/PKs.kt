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
import kotlin.math.max


class PKs(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { PKs(testKurswahl, testFachdata) }
        }

        class MyComboBox(model: ExclusiveComboBoxModel) : JComboBox<Fach>(model) {
            override fun getSelectedIndex(): Int = max(super.getSelectedIndex(), 0)

            override fun getSelectedItem(): Fach? {
                return super.getSelectedItem() as Fach?
            }
        }
    }

    private val pk1: MyComboBox
    private val pk2: MyComboBox
    private val pk3: MyComboBox
    private val pk4: MyComboBox


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
        pk1 = MyComboBox(model1)

        val model2 = ExclusiveComboBoxModel(fachData.faecher, pk1)
        pk2 = MyComboBox(model2)

        val model3 = ExclusiveComboBoxModel(fachData.faecher, pk2)
        pk3 = MyComboBox(model3)

        val model4 = ExclusiveComboBoxModel(fachData.faecher, pk3)
        pk4 = MyComboBox(model4)


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

        pk1.renderer = renderer
        pk2.renderer = renderer
        pk3.renderer = renderer
        pk4.renderer = renderer


        // Daten einsetzen
        pk1.selectedItem = wahlData.lk1
        pk2.selectedItem = wahlData.lk2
        pk3.selectedItem = wahlData.pf3
        pk4.selectedItem = wahlData.pf4


        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(pk1, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pk2, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pk3, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pk4, row = 4, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }
    }


    override fun close(): KurswahlData {


        return wahlData.copy(lk1 = pk1.selectedItem, lk2 = pk2.selectedItem)
    }

    override fun isDataValid(): Boolean {
        return (pk1.selectedItem != null && pk2.selectedItem != null)
    }

    override val windowName: String
        get() = "Fremdsprachen & Wahlpflichtfächer"
}