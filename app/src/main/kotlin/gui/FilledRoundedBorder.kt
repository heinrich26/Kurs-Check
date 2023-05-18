/*
 * Copyright (c) 2023  Hendrik Horstmann
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
import com.kurswahlApp.data.transparentise
import org.jetbrains.annotations.NotNull
import java.awt.*
import javax.swing.border.Border

class FilledRoundedBorder(
    padding: Insets,
    @NotNull private val border: Color?,
    @NotNull private val background: Color?
) : Border {
    private val padding = Insets(padding.top+4, padding.left+4,padding.bottom+4, padding.right+4)
    private val dx = padding.left + padding.right
    private val dy = padding.top + padding.bottom
    private val x = padding.left
    private val y = padding.top

    /**
     * Creates a filled, rounded shape using the foreground-color of the component, where the border has 50% opacity,
     * the background 25%.
     */
    constructor(padding: Insets) : this(padding, null, null)

    /**
     * Creates a filled, rounded shape using the given color, where the border has 50% opacity, the background 25%.
     */
    constructor(padding: Insets, color: Color) : this(padding, color.transparentise(127), color.transparentise(63))

    override fun paintBorder(c: Component, g: Graphics, x2: Int, y2: Int, width: Int, height: Int) {
        with(g as Graphics2D) {
            setRenderingHints(RENDERING_HINTS)
            color = this@FilledRoundedBorder.background ?: c.foreground.transparentise(63)
            fillRoundRect(x, y, width - dx, height - dy, 12, 12)
            color = this@FilledRoundedBorder.border ?: c.foreground.transparentise(127)
            drawRoundRect(x, y, width - dx, height - dy, 12, 12)
        }
    }

    override fun getBorderInsets(c: Component?) = padding

    override fun isBorderOpaque() = false

}
