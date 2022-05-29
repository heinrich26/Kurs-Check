package gui

import gui.Consts.COLOR_ON_BACKGROUND
import gui.Consts.COLOR_ON_BACKGROUND_DISABLED
import gui.Consts.COLOR_PRIMARY
import gui.Consts.FONT_NAME
import gui.Consts.SIDEBAR_SIZE
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

class SidebarLabel(private val lText: String, clickEvent: () -> Unit) : ClickableDestionation(clickEvent = clickEvent) {
    init {
        font = Font(FONT_NAME, Font.BOLD, 24)
    }

    private val tLen = getFontMetrics(font).stringWidth(lText)
    private val tHeight = 18
    private val insetX: Int = (SIDEBAR_SIZE - tLen) / 2
    private val insetY: Int = (SIDEBAR_SIZE - tHeight) / 2 + tHeight

    override fun paintComponent(g: Graphics) {
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

        g2D.color = if (isSelected) COLOR_PRIMARY else if (isEnabled) COLOR_ON_BACKGROUND else COLOR_ON_BACKGROUND_DISABLED
        g2D.drawString(lText, insetX, insetY)
    }
}