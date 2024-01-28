/*
 * Copyright (c) 2022-2024  Hendrik Horstmann
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

import com.kurswahlApp.data.Consts.RENDERING_HINTS
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.border.Border


class RoundedBorder(private val radius: Int, private val color: Color? = null) : Border {
    override fun getBorderInsets(c: Component?) = Insets(radius / 2)

    override fun isBorderOpaque() = false


    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        if (color != null) g.color = color
        with(g as Graphics2D) {
            setRenderingHints(RENDERING_HINTS)
            drawRoundRect(x + 1, y + 1, width - 2, height - 2, radius, radius)
        }
    }
}