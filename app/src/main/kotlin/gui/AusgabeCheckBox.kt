/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
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

import com.kurswahlApp.R
import com.kurswahlApp.data.Consts.CHECKBOX_CHECKED
import com.kurswahlApp.data.Consts.CHECKBOX_LK
import com.kurswahlApp.data.Consts.CHECKBOX_PF3
import com.kurswahlApp.data.Consts.CHECKBOX_PF4
import com.kurswahlApp.data.Consts.CHECKBOX_PF5
import com.kurswahlApp.data.Consts.RENDERING_HINTS
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
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
        preferredSize = 24 by 24
        minimumSize = preferredSize
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D

        g2.addRenderingHints(RENDERING_HINTS)

        if (style == STYLE.UNCHECKED) {
            g2.color = UNCHECKED_COLOR
            g2.stroke = BasicStroke(1.5f)
            g2.draw(RoundRectangle2D.Double(1.0, 1.0, 22.0, 22.0, 9.0, 9.0))
        } else {
            g2.color = if (style == STYLE.UNAVAILABLE) UNCHECKED_COLOR else CHECKED_COLOR
            g2.fill(
                when (style) {
                    STYLE.CHECKED -> CHECKBOX_CHECKED
                    STYLE.LK -> CHECKBOX_LK
                    STYLE.PF3 -> CHECKBOX_PF3
                    STYLE.PF4 -> CHECKBOX_PF4
                    STYLE.UNAVAILABLE -> R.checkbox_unavailable
                    else /*PF5*/ -> CHECKBOX_PF5
                }
            )
        }
    }

    enum class STYLE {
        UNCHECKED, UNAVAILABLE, CHECKED, LK, PF3, PF4, PF5
    }

    companion object {
        private val UNCHECKED_COLOR = Color(139, 139, 139)
        private val CHECKED_COLOR = Color(0, 103, 192)
    }
}