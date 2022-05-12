package gui

import java.awt.Color
import java.awt.Component
import java.awt.Graphics

import java.awt.Insets

import javax.swing.border.Border


class RoundedBorder(private val radius: Int) : Border {
    override fun getBorderInsets(c: Component?): Insets {
        return Insets(radius + 1, radius + 1, radius + 2, radius)
    }

    override fun isBorderOpaque(): Boolean {
        return true
    }

    override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        g.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
    }
}