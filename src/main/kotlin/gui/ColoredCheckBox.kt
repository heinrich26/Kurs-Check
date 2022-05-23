package gui

import gui.Consts.CHECKBOX_HEIGHT
import gui.Consts.CHECKBOX_WIDTH
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JToggleButton
import javax.swing.border.EmptyBorder

/**
 * This is a Checkbox, that doesnt expect Borders!
 */
class ColoredCheckBox: JToggleButton() {
    init {
        preferredSize = Dimension(CHECKBOX_WIDTH, CHECKBOX_HEIGHT)
    }

    override fun paintComponent(g: Graphics) {
        val g2D = g as Graphics2D

        g.color = Color.RED

        g2D.drawRect(0,0, width-1, height-1)
    }
}