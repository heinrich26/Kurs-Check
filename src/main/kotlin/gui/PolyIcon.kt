package gui

import gui.Consts.COLOR_CONTROL
import gui.Consts.COLOR_ON_BACKGROUND
import gui.Consts.COLOR_ON_BACKGROUND_DISABLED
import gui.Consts.COLOR_PRIMARY
import gui.Consts.SIDEBAR_SIZE
import java.awt.*
import java.awt.geom.AffineTransform

class PolyIcon(poly: Polygon, defaultEnabled: Boolean, clickEvent: () -> Unit) :
    ClickableDestionation(defaultEnabled, clickEvent = clickEvent) {
    companion object {
        @JvmStatic
        val transform1 = AffineTransform(1.0, 0.0, 0.0, 1.0, -12.0, -12.0)
        val transform2 = (SIDEBAR_SIZE / 36.0).let { AffineTransform(it, 0.0, 0.0, it, 0.0, 0.0) }
        val transform3 = (SIDEBAR_SIZE / 2.0).let { AffineTransform(1.0, 0.0, 0.0, 1.0, it, it) }
    }

    private val shape =
        transform3.createTransformedShape(transform2.createTransformedShape(transform1.createTransformedShape(poly)))

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

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