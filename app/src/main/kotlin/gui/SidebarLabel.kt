package gui

import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND
import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND_DISABLED
import com.kurswahlApp.data.Consts.COLOR_PRIMARY
import com.kurswahlApp.data.Consts.FONT_NAME
import com.kurswahlApp.data.Consts.RENDERING_HINTS
import com.kurswahlApp.data.Consts.SIDEBAR_SIZE
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D

class SidebarLabel(private val lText: String, defaultEnabled: Boolean = false, clickEvent: () -> Unit) :
    ClickableDestionation(defaultEnabled = defaultEnabled, clickEvent = clickEvent) {

    init {
        font = Font(FONT_NAME, Font.BOLD, 24)
    }

    private val tLen = getFontMetrics(font).stringWidth(lText)
    private val tHeight = 18
    private val insetX: Int = (SIDEBAR_SIZE - tLen) / 2
    private val insetY: Int = (SIDEBAR_SIZE - tHeight) / 2 + tHeight

    override fun paintComponent(g: Graphics) {
        val g2D = g as Graphics2D

        g2D.setRenderingHints(RENDERING_HINTS)

        if (hasFocus) {
            g2D.color = Consts.COLOR_CONTROL
            g2D.fillOval(10, 10, width - 20, height - 20)
        }

        g2D.color =
            if (isSelected) COLOR_PRIMARY else if (isEnabled) COLOR_ON_BACKGROUND else COLOR_ON_BACKGROUND_DISABLED
        g2D.drawString(lText, insetX, insetY)
    }
}