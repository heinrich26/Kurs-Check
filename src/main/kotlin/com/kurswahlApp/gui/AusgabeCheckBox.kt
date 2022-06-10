package com.kurswahlApp.gui

import com.kurswahlApp.gui.Consts.CHECKBOX_CHECKED
import com.kurswahlApp.gui.Consts.CHECKBOX_LK
import com.kurswahlApp.gui.Consts.CHECKBOX_PF3
import com.kurswahlApp.gui.Consts.CHECKBOX_PF4
import com.kurswahlApp.gui.Consts.CHECKBOX_PF5
import com.kurswahlApp.gui.Consts.RENDERING_HINTS
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent

class AusgabeCheckBox(style: STYLE = STYLE.UNCHECKED) : JComponent() {
    var style: STYLE = style
        set(value) {
            field = value
            repaint()
        }

    init {
        isOpaque = false
        preferredSize = Dimension(24, 24)
        minimumSize = preferredSize
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D

        g2.addRenderingHints(RENDERING_HINTS)

        if (style == STYLE.UNCHECKED) {
            g2.color = Color(139, 139, 139)
            g2.stroke = BasicStroke(1.5f)
            g2.draw(RoundRectangle2D.Double(1.0, 1.0, 22.0, 22.0, 9.0, 9.0))
        } else {
            g2.color = Color(0, 103, 192)
            g2.fill(
                when (style) {
                    STYLE.NORMAL -> CHECKBOX_CHECKED
                    STYLE.LK -> CHECKBOX_LK
                    STYLE.PF3 -> CHECKBOX_PF3
                    STYLE.PF4 -> CHECKBOX_PF4
                    else /*PF5*/ -> CHECKBOX_PF5
                }
            )
        }
    }

    enum class STYLE {
        UNCHECKED, NORMAL, LK, PF3, PF4, PF5
    }
}