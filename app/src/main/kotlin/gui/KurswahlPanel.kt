package gui

import com.kurswahlApp.data.Consts.PANEL_HEIGHT
import com.kurswahlApp.data.Consts.PANEL_WIDTH
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.UIManager

/**
 * Ein Panel mit Funktionalität für die KurswahlApp
 *
 * @property wahlData die aktuelle Kurswahl
 * @property fachData FachData der Session
 * @property notifier Callback, das meldet ob die Daten im KurswahlPanel gültig sind
 */
abstract class KurswahlPanel(val wahlData: KurswahlData, val fachData: FachData, val notifier: (isValid: Boolean) -> Unit) : JPanel(GridBagLayout()) {
    abstract fun close() : KurswahlData

    abstract fun isDataValid(): Boolean

    abstract val windowName: String

    init {
        preferredSize = Dimension(PANEL_WIDTH, PANEL_HEIGHT)
        isOpaque = false
    }

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