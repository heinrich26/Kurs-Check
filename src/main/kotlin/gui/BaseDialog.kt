package gui

import add
import data.FachData
import data.KurswahlData
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.*
import javax.swing.*

class BaseDialog(wahlData: KurswahlData, fachData: FachData) : JDialog() {
    private lateinit var contentPane: JPanel
    private lateinit var buttonBack: JButton
    private lateinit var buttonCancel: JButton
    private lateinit var buttonNext: JButton

    override fun getTitle(): String = "Base Dialog"

    private fun onBack() {
        // add your code here
        dispose()
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    private fun setupDialog() {
        contentPane = JPanel()
        contentPane.layout = GridBagLayout()
        val panel1 = JPanel()
        panel1.layout = GridBagLayout()
        contentPane.add(
            panel1,
            margin = Insets(10, 10, 10, 10),
            row = 0, column = 0, anchor = GridBagConstraints.CENTER,
            fill = GridBagConstraints.BOTH,
            weightx = 1.0, weighty = 1.0
        )
        val panel2 = JPanel()
        panel2.layout = GridBagLayout()
        contentPane.add(
            panel2,
            margin = Insets(10, 10, 10, 10),
            row = 1,
            column = 0,
            fill = GridBagConstraints.HORIZONTAL,
            anchor = GridBagConstraints.CENTER,
            weightx = 1.0
        )
        buttonNext = JButton("Next")
        buttonBack = JButton("Back")
        buttonCancel = JButton("Cancel")
        buttonNext.preferredSize = buttonCancel.preferredSize
        buttonBack.preferredSize = buttonCancel.preferredSize
        panel2.add(buttonCancel, column = 0, weightx = 1.0, anchor = GridBagConstraints.WEST)
        panel2.add(buttonBack, column = 1)
        panel2.add(buttonNext, column = 2)
    }

    fun getRootComponent(): JComponent = contentPane

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            SwingUtilities.invokeLater { createAndShowGUI() }
        }

        fun createAndShowGUI() {
            val dialog = BaseDialog(KurswahlData(), FachData(emptyList(), emptyList(), emptyList()))
            dialog.pack()
            dialog.isVisible = true
        }
    }

    init {
        setDefaultLookAndFeelDecorated(true)
        setupDialog()
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonBack
        buttonBack.addActionListener { onBack() }
        buttonCancel.addActionListener { onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })
    }
}