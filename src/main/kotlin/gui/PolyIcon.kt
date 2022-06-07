package gui

import gui.Consts.COLOR_CONTROL
import gui.Consts.COLOR_ON_BACKGROUND
import gui.Consts.COLOR_ON_BACKGROUND_DISABLED
import gui.Consts.COLOR_PRIMARY
import gui.Consts.RENDERING_HINTS
import gui.Consts.SIDEBAR_SIZE
import java.awt.BasicStroke
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform

class PolyIcon(poly: Shape, defaultEnabled: Boolean, clickEvent: () -> Unit) :
    ClickableDestionation(defaultEnabled,  clickEvent = clickEvent) {
    companion object {
        val transform1 = AffineTransform(1.0, 0.0, 0.0, 1.0, -12.0, -12.0)
        val transform2 = (SIDEBAR_SIZE / 36.0).let { AffineTransform(it, 0.0, 0.0, it, 0.0, 0.0) }
        val transform3 = (SIDEBAR_SIZE / 2.0).let { AffineTransform(1.0, 0.0, 0.0, 1.0, it, it) }
    }

    private val shape =
        transform3.createTransformedShape(transform2.createTransformedShape(transform1.createTransformedShape(poly)))

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2D = g as Graphics2D

        g2D.setRenderingHints(RENDERING_HINTS)

        if (hasFocus) {
            g2D.color = COLOR_CONTROL
            g2D.fillOval(10, 10, width - 20, height - 20)
        }

        g2D.color = if (isSelected) COLOR_PRIMARY
        else if (isEnabled) COLOR_ON_BACKGROUND
        else COLOR_ON_BACKGROUND_DISABLED

        g2D.stroke = BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2D.draw(shape)
        g2D.fill(shape)
    }
}