package gui

import gui.Consts.RENDERING_HINTS
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import javax.swing.border.Border


class RoundedBorder(private val radius: Int) : Border {
    override fun getBorderInsets(c: Component?): Insets {
        return Insets(radius + 1, radius + 1, radius + 2, radius)
    }

    override fun isBorderOpaque(): Boolean {
        return true
    }

    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        with(g as Graphics2D) {
            setRenderingHints(RENDERING_HINTS)
            drawRoundRect(x + 1, y + 1, width - 2, height - 2, radius, radius)
        }
    }
}