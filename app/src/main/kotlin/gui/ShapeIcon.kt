/*
 * Copyright (c) 2023-2025  Hendrik Horstmann
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
import java.awt.*
import javax.swing.Icon

class ShapeIcon(private val shape: Shape, val size: Int, var color: Color? = null) : Icon {
    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        with(g as Graphics2D) {
            setRenderingHints(RENDERING_HINTS)

            color = this@ShapeIcon.color ?: c.foreground
            translate(x, y)
            fill(shape)
            translate(-x, -y)

        }
    }

    override fun getIconWidth(): Int = size

    override fun getIconHeight(): Int = size

}