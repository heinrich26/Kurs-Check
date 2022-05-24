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


class Fremdsprachen(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Fremdsprachen(testKurswahl, testFachdata) }
        }

        class MyComboBox(model: ExclusiveComboBoxModel) : JComboBox<Fach>(model) {
            override fun getSelectedIndex(): Int = max(super.getSelectedIndex(), 0)

            override fun getSelectedItem(): Fach? {
                return super.getSelectedItem() as Fach?
            }
        }
    }

    private val fsJahr1: SpinnerNumberModel
    private val fsJahr2: SpinnerNumberModel
    private val fsJahr3: SpinnerNumberModel
    private val fsJahr4: SpinnerNumberModel

    private val fs1: MyComboBox
    private val fs2: MyComboBox
    private val fs3: MyComboBox
    private val fs4: MyComboBox
    private val wpf1: MyComboBox
    private val wpf2: MyComboBox

    init {
        this.layout = GridBagLayout()

        add(
            JLabel("Fremdsprachen:"),
            column = 0,
            columnspan = 2,
            margin = Insets(0, 0, 4, 0),
            anchor = GridBagConstraints.WEST
        )
        add(Box.createHorizontalStrut(50), column = 2)
        add(JLabel("ab Kl.:"), column = 3, anchor = GridBagConstraints.NORTHWEST)
        add(
            JLabel("Wahlpflicht:"),
            row = 5,
            column = 0,
            columnspan = 2,
            anchor = GridBagConstraints.WEST,
            margin = Insets(10, 0, 4, 0)
        )

//        fsJahr4 = ChainedSpinnerNumberModel(1, 1, 10)
//        fsJahr3 = ChainedSpinnerNumberModel(1, 1, 10, fsJahr4)
//        fsJahr2 = ChainedSpinnerNumberModel(1, 1, 10, fsJahr3)
//        fsJahr1 = ChainedSpinnerNumberModel(1, 1, 10, fsJahr2)

        fsJahr4 = SpinnerNumberModel(1, 1, 10,1)
        fsJahr3 = SpinnerNumberModel(1, 1, 10,1)
        fsJahr2 = SpinnerNumberModel(1, 1, 10,1)
        fsJahr1 = SpinnerNumberModel(1, 1, 10,1)

        val spinner3 = JSpinner(fsJahr3)
        val spinner4 = JSpinner(fsJahr4)
        spinner3.isEnabled = false
        spinner4.isEnabled = false

        add(JSpinner(fsJahr1), row = 1, column = 3)
        add(JSpinner(fsJahr2), row = 2, column = 3)
        add(spinner3, row = 3, column = 3)
        add(spinner4, row = 4, column = 3)


        val model1 = ExclusiveComboBoxModel(fachData.fremdsprachen)
        fs1 = MyComboBox(model1)


        val model2 = ExclusiveComboBoxModel(fachData.fremdsprachen, fs1)
        fs2 = MyComboBox(model2)

        val model3 = ExclusiveComboBoxModel(fachData.fremdsprachen, fs2)
        fs3 = MyComboBox(model3)
        fs3.isEnabled = false

        val model4 = ExclusiveComboBoxModel(fachData.fremdsprachen, fs3)
        fs4 = MyComboBox(model4)
        fs4.isEnabled = false


        val listener3n4: (ActionEvent) -> Unit = { event ->
            if (event.actionCommand == "comboBoxChanged") {
                model4.updateData()
                (model2.selectedItem != null).let {
                    fs3.isEnabled = it
                    spinner3.isEnabled = it
                }
                (model3.selectedItem != null).let {
                    fs4.isEnabled = it
                    spinner4.isEnabled = it
                }
            }
        }
        fs1.addActionListener(listener3n4)
        fs2.addActionListener(listener3n4)
        fs3.addActionListener { event ->
            if (event.actionCommand == "comboBoxChanged") {
                model4.updateData()
                (model3.selectedItem != null).let {
                    fs4.isEnabled = it
                    spinner4.isEnabled = it
                }
            }
        }
        fs4.addActionListener { if (it.actionCommand == "comboBoxChanged") model4.updateData() }

        for (i in 1..4) {
            add(JLabel("$i."), row = i, column = 0)
        }

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

        fs1.renderer = renderer
        fs2.renderer = renderer
        fs3.renderer = renderer
        fs4.renderer = renderer

        // Daten einsetzen
        wahlData.fremdsprachen.let { fs ->
            val nFS = fs.size
            if (nFS != 0) { // Case: 1+ Items
                fs[0].let {
                    fs1.selectedItem = it.first
                    fsJahr1.value = it.second
                }
                if (nFS != 1) { // Case: 2+ Items
                    fs[1].let {
                        fs2.selectedItem = it.first
                        fsJahr2.value = it.second
                    }
                    if (nFS != 2) { // Case: 3+ Items
                        fs[2].let {
                            fs3.selectedItem = it.first
                            fsJahr3.value = it.second
                        }
                        if (nFS != 3) // Case: 4 Items
                            fs[3].let {
                                fs4.selectedItem = it.first
                                fsJahr4.value = it.second
                            }
                    }
                }
            }
        }


        val wpfModel1 = ExclusiveComboBoxModel(fachData.wpfs)
        wpf1 = MyComboBox(wpfModel1)
        wpf1.renderer = renderer
        val wpfModel2 = ExclusiveComboBoxModel(fachData.wpfs, wpf1)
        wpf2 = MyComboBox(wpfModel2)
        wpf2.isEnabled = false
        wpf2.renderer = renderer

        wpf1.addActionListener { if (it.actionCommand == "comboBoxChanged") wpfModel2.updateData() }

        val checker = JCheckBox()
        checker.addActionListener {
            if (!checker.isSelected) {
                wpf2.isEnabled = false
                wpf2.selectedIndex = 0
            } else wpf2.isEnabled = true
        }

        // Daten einsetzen
        wahlData.wpfs?.let {
            wpf1.selectedItem = it.first
            wpf2.selectedItem = it.second
        }

        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(fs1, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(fs2, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(fs3, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(fs4, row = 4, column = 1, fill = GridBagConstraints.BOTH, margin = it)

            add(wpf1, row = 6, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(wpf2, row = 7, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }
        add(checker, row = 7, column = 0, anchor = GridBagConstraints.EAST)
    }


    override fun close(): KurswahlData {
        val sprachen: MutableList<Pair<Fach, Int>> =
            mutableListOf(fs1.selectedItem!! to fsJahr1.number as Int, fs2.selectedItem!! to fsJahr2.number as Int)
        fs3.selectedItem.let { sel ->
            if (sel != null) {
                sprachen.add(sel to fsJahr3.number as Int)
                fs4.selectedItem.let { if (it != null) sprachen.add(it to fsJahr4.number as Int) }
            }
        }

        return wahlData.copy(fremdsprachen = sprachen, wpfs = wpf1.selectedItem!! to wpf2.selectedItem)
    }

    override fun isDataValid(): Boolean {
        return (fs1.selectedItem != null && fs2.selectedItem != null && wpf1.selectedItem != null)
    }

    override val windowName: String
        get() = "Fremdsprachen & Wahlpflichtfächer"
}