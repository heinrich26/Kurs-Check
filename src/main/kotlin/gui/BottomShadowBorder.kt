package gui

import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.border.EmptyBorder

class BottomShadowBorder(val pixels: Int) : EmptyBorder(0, 0, pixels, 0) {
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val topOpacity = 80
        val m: Int = -topOpacity / pixels

        g.color = c.background
        g.fillRect(x, y, width, height - pixels)

        for (i in 0 until pixels) {
            g.color = Color(0, 0, 0, m * i + topOpacity)
            g.drawLine(0, i + height - pixels, width, i + height - pixels)
        }
    }

    override fun isBorderOpaque(): Boolean = false
}