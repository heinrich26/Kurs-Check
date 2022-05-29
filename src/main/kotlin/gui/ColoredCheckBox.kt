package gui

import gui.Consts.CHECKBOX_HEIGHT
import gui.Consts.CHECKBOX_WIDTH
import java.awt.*
import javax.swing.JToggleButton

/**
 * This is a Checkbox, that doesnt expect Borders!
 */
class ColoredCheckBox: JToggleButton() {
    init {
        preferredSize = Dimension(CHECKBOX_WIDTH, CHECKBOX_HEIGHT)
    }

    override fun paintComponent(g: Graphics) {
        val g2D = g as Graphics2D

        g2D.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        g2D.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        )
        g2D.setRenderingHint(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_PURE
        )

        g.color = Color.RED

        g2D.stroke = BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        if (isSelected) {
            g2D.drawLine(0, 0, width - 1, height - 1)
            g2D.drawLine(0, height - 1, width - 1, 0)
        }
    }
}