package data

import gui.Consts
import gui.Consts.COLOR_ON_BACKGROUND
import gui.Consts.COLOR_PRIMARY
import gui.Consts.FONT_NAME
import gui.Consts.SIDEBAR_SIZE
import gui.Focusable
import java.awt.*
import javax.swing.JComponent

class SidebarLabel(private val text: String): JComponent(), Focusable {
    private val tLen: Int
    private val tHeight = 18

    init {
        font = Font(FONT_NAME, Font.BOLD, 24)

        tLen = getFontMetrics(font).stringWidth(text)

        Dimension(SIDEBAR_SIZE, SIDEBAR_SIZE).let {
            minimumSize = it
            preferredSize = it
        }
    }

    private val insetX: Int = (SIDEBAR_SIZE - tLen) / 2
    private val insetY: Int = (SIDEBAR_SIZE - tHeight) / 2 + tHeight

    override fun paintComponent(g: Graphics) {
        println("fw: $tLen fh: $tHeight")
        val g2D = g as Graphics2D

        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON)
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY)
        g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_PURE)

        if (hasFocus) {
            g2D.color = Consts.COLOR_CONTROL
            g2D.fillOval(10, 10, width-20, height-20)
        }

        g2D.color = if (isEnabled) COLOR_PRIMARY else COLOR_ON_BACKGROUND
        g2D.drawString(text, insetX, insetY)
    }

    override var hasFocus: Boolean = false
        set(value) {
            field = value
            repaint()
        }
}