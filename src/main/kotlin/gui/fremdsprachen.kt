package gui

import ExclusiveComboBoxModel
import add
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.event.ListDataListener


class Fremdsprachen : JPanel() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            val panel = Fremdsprachen()
            val frame = JFrame("Fremdsprachen")
            frame.contentPane = panel

            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(300, 300)
            frame.setLocation(430, 100)
            frame.isVisible = true
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

        val model1 = ExclusiveComboBoxModel(MainScreen.faecher, emptyArray())
        val fs1 = JComboBox(model1)

        val model2 = ExclusiveComboBoxModel(MainScreen.faecher, arrayOf(fs1))
        val fs2 = JComboBox(model2)

        val model3 = ExclusiveComboBoxModel(MainScreen.faecher, arrayOf(fs1, fs2))
        val fs3 = JComboBox(model3)

        val model4 = ExclusiveComboBoxModel(MainScreen.faecher, arrayOf(fs1, fs2, fs3))
        val fs4 = JComboBox(model4)

        fs3.selectedIndex = 2

        for (i in 1..4) {
            add(JLabel("$i."), row = i, column = 0)
        }

        add(fs1, row = 1, column = 1)
        add(fs2, row = 2, column = 1)
        add(fs3, row = 3, column = 1)
        add(fs4, row = 4, column = 1)


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

        add(wpf1, row = 6, column = 1)
        add(wpf2, row = 7, column = 1)
        add(checker, row = 7, column = 0)
    }
}