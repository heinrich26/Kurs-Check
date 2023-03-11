/*
 * Copyright (c) 2022  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kurswahlApp.gui

import com.kurswahlApp.data.Consts.CHECKBOX_CHECKED
import com.kurswahlApp.data.Consts.CHECKBOX_LK
import com.kurswahlApp.data.Consts.CHECKBOX_PF3
import com.kurswahlApp.data.Consts.CHECKBOX_PF4
import com.kurswahlApp.data.Consts.CHECKBOX_PF5
import com.kurswahlApp.data.Consts.RENDERING_HINTS
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