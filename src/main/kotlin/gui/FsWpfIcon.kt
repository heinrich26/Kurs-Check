package gui

import gui.Consts.COLOR_ON_BACKGROUND
import gui.Consts.COLOR_PRIMARY
import gui.Consts.FONT_NAME
import java.awt.*
import javax.swing.JComponent

class FsWpfIcon(
    private val defaultColor: Color = COLOR_ON_BACKGROUND,
    private val activeColor: Color = COLOR_PRIMARY
) : JComponent(), Focusable {

    companion object {
        @JvmStatic
        val bigFont = Font(FONT_NAME, Font.BOLD, 18)

        @JvmStatic
        val smallFont = Font(FONT_NAME, Font.BOLD, 15)
    }

    private val fsLen: Int
    private val wpfLen: Int

    init {
        Dimension(Consts.SIDEBAR_SIZE, Consts.SIDEBAR_SIZE).let {
            minimumSize = it
            preferredSize = it
        }

        getFontMetrics(bigFont).let {
            fsLen = it.stringWidth("FS")
            wpfLen = it.stringWidth("WPF")
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2D = g as Graphics2D
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON)
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY)
        g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_PURE)

        if (hasFocus) {
            g2D.color = Consts.COLOR_CONTROL
            g2D.fillOval(5, 5, width-10, height-10)
        }

        // Hintergrund nur malen, wenn Ausgewählt
        if (isEnabled) {
            g2D.color = activeColor
            g2D.fillRoundRect(5, 5, width-10, height-10, 24, 24)
            g2D.color = g2D.background
        } else g2D.color = defaultColor

        g2D.font = bigFont

        g2D.drawString("FS", 10, 36)
        g2D.drawString("WPF", 18, 54)
        g2D.font = smallFont
        g2D.drawString("s", 10 + fsLen, 36)
        g2D.drawString("s", 18 + wpfLen, 54)
    }

    override var hasFocus: Boolean = false
        set(value) {
            field = value
            repaint()
        }
}