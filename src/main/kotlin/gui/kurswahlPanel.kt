package gui

import data.FachData
import data.KurswahlData
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.UIManager

abstract class KurswahlPanel(val wahlData: KurswahlData, val fachData: FachData) : JPanel() {
    abstract fun close() : KurswahlData

    abstract val windowName: String

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            val panel = JPanel()
            val frame = JFrame()
            frame.contentPane = panel

            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(300, 300)
            frame.setLocation(430, 100)
            frame.isVisible = true
        }
    }
}