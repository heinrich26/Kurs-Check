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


class Fremdsprachen(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest(Fremdsprachen(testKurswahl, testFachdata))
        }

        val choices = arrayOf(
            "CHOICE 1", "CHOICE 2", "CHOICE 3", "CHOICE 4",
            "CHOICE 5", "CHOICE 6"
        )
    }

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
        add(JLabel("ab Jg.:"), column = 3, anchor = GridBagConstraints.NORTHWEST)
        add(
            JLabel("Wahlpflicht:"),
            row = 5,
            column = 0,
            columnspan = 2,
            anchor = GridBagConstraints.WEST,
            margin = Insets(10, 0, 4, 0)
        )

        val value: SpinnerModel = SpinnerNumberModel(1, 1, 10, 1)
        val value2: SpinnerModel = SpinnerNumberModel(1, 1, 10, 1)
        val value3: SpinnerModel = SpinnerNumberModel(1, 1, 10, 1)
        val value4: SpinnerModel = SpinnerNumberModel(1, 1, 10, 1)

        add(JSpinner(value), row = 1, column = 3)
        add(JSpinner(value2), row = 2, column = 3)
        add(JSpinner(value3), row = 3, column = 3)
        add(JSpinner(value4), row = 4, column = 3)

        val model1 = ExclusiveComboBoxModel(fachData.fremdsprachen, emptyArray())
        val fs1 = JComboBox(model1)

        val model2 = ExclusiveComboBoxModel(fachData.fremdsprachen, arrayOf(fs1))
        val fs2 = JComboBox(model2)

        val model3 = ExclusiveComboBoxModel(fachData.fremdsprachen, arrayOf(fs1, fs2))
        val fs3 = JComboBox(model3)

        val model4 = ExclusiveComboBoxModel(fachData.fremdsprachen, arrayOf(fs1, fs2, fs3))
        val fs4 = JComboBox(model4)

//        fs3.selectedIndex = 2

        for (i in 1..4) {
            add(JLabel("$i."), row = i, column = 0)
        }

        val renderer = object: DefaultListCellRenderer() {
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
                ) //To change body of generated methods, choose Tools | Templates.
        }

        fs1.renderer = renderer
        fs2.renderer = renderer
        fs3.renderer = renderer
        fs4.renderer = renderer

        add(fs1, row = 1, column = 1, fill=GridBagConstraints.BOTH)
        add(fs2, row = 2, column = 1, fill=GridBagConstraints.BOTH)
        add(fs3, row = 3, column = 1, fill=GridBagConstraints.BOTH)
        add(fs4, row = 4, column = 1, fill=GridBagConstraints.BOTH)


        val wpf1 = JComboBox(choices)
        val wpf2 = JComboBox(choices)
        wpf2.isEnabled = false

        val checker = JCheckBox()
        checker.addActionListener {
            if (!checker.isSelected) {
                wpf2.isEnabled = false
                wpf2.selectedIndex = 0
            } else wpf2.isEnabled = true
        }

        add(wpf1, row = 6, column = 1, fill=GridBagConstraints.BOTH)
        add(wpf2, row = 7, column = 1, fill=GridBagConstraints.BOTH)
        add(checker, row = 7, column = 0, anchor = GridBagConstraints.EAST)
    }

    override fun close(): KurswahlData {
        TODO("Not yet implemented")
    }

    override val windowName: String
        get() = TODO("Not yet implemented")
}