package com.kurswahlApp.gui

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextField


class HintTextField(private val hint: String, text: String? = null, columns: Int = 0) : JTextField(null, text, columns) {
    override fun paint(g: Graphics) {
        super.paint(g)
        if (text.isEmpty()) {
            val h = height
            (g as Graphics2D).setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )
            val ins = insets
            val fm = g.getFontMetrics()
            val c0 = background.rgb
            val c1 = foreground.rgb
            val m = -0x1010102
            val c2 = (c0 and m ushr 1) + (c1 and m ushr 1)
            g.setColor(Color(c2, true))
            g.drawString(hint, ins.left + 2, h / 2 + fm.ascent / 2 - 2)
        }
    }
}