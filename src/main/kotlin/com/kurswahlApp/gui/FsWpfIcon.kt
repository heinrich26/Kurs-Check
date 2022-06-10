package com.kurswahlApp.gui

import com.kurswahlApp.gui.Consts.COLOR_CONTROL
import com.kurswahlApp.gui.Consts.COLOR_ON_BACKGROUND
import com.kurswahlApp.gui.Consts.COLOR_ON_BACKGROUND_DISABLED
import com.kurswahlApp.gui.Consts.COLOR_PRIMARY
import com.kurswahlApp.gui.Consts.FONT_NAME
import com.kurswahlApp.gui.Consts.RENDERING_HINTS
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D

class FsWpfIcon(clickEvent: () -> Unit) : ClickableDestionation(clickEvent = clickEvent) {

    companion object {
        @JvmStatic
        val bigFont = Font(FONT_NAME, Font.BOLD, 18)

        @JvmStatic
        val smallFont = Font(FONT_NAME, Font.BOLD, 15)
    }

    private val fsLen: Int
    private val wpfLen: Int

    init {
        getFontMetrics(bigFont).let {
            fsLen = it.stringWidth("FS")
            wpfLen = it.stringWidth("WPF")
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2D = g as Graphics2D
        g2D.setRenderingHints(RENDERING_HINTS)

        if (hasFocus) {
            g2D.color = COLOR_CONTROL
            g2D.fillOval(5, 5, width - 10, height - 10)
        }

        // Hintergrund nur malen, wenn Ausgewählt

        g2D.color = if (isSelected) {
            COLOR_PRIMARY
            /*g2D.fillRoundRect(5, 5, width-10, height-10, 24, 24)
            g2D.color = g2D.background*/
        } else if (isEnabled) COLOR_ON_BACKGROUND
        else COLOR_ON_BACKGROUND_DISABLED

        g2D.font = bigFont

        g2D.drawString("FS", 10, 36)
        g2D.drawString("WPF", 18, 54)
        g2D.font = smallFont
        g2D.drawString("s", 10 + fsLen, 36)
        g2D.drawString("s", 18 + wpfLen, 54)
    }
}