package gui

import gui.Consts.COLOR_CONTROL
import gui.Consts.SIDEBAR_SIZE
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.geom.AffineTransform
import javax.swing.JComponent

class PolyIcon(private val poly: Polygon) : JComponent(), Focusable {
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
            g2D.fillOval(10, 10, width-20, height-20)
        }

        g2D.color = if (isEnabled) Consts.COLOR_PRIMARY else Consts.COLOR_ON_BACKGROUND

        g2D.stroke = BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2D.draw(shape)
        g2D.fill(shape)
    }

    init {
        Dimension(SIDEBAR_SIZE, SIDEBAR_SIZE).let {
            minimumSize = it
            preferredSize = it
        }
    }

    override var hasFocus: Boolean = false
        set(value) {
            field = value
            repaint()
        }
}