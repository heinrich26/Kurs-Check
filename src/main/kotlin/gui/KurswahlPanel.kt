package gui

import data.FachData
import data.KurswahlData
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.UIManager

abstract class KurswahlPanel(val wahlData: KurswahlData, val fachData: FachData) : JPanel() {
    abstract fun close() : KurswahlData

    abstract fun isDataValid(): Boolean

    open fun cancel() {}

    abstract val windowName: String

    companion object {
        @JvmStatic
        fun runTest(gen: () -> KurswahlPanel) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            val frame = JFrame()
            frame.contentPane = gen()

            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setSize(300, 300)
            frame.setLocation(430, 100)
            frame.isVisible = true
        }
    }
}