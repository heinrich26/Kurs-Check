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

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

/**
 * Erstellt einen abgerundeten Hintergrund mit der gegebenen Farbe
 *
 * @param color Farbe dieses Hintergrunds
 */
class SolidFiller(color: Color) : JComponent() {
    companion object {
        val minSize = 0 by 0
        val maxSize = Short.MAX_VALUE.toInt() by Short.MAX_VALUE.toInt()
    }

    init {
        background = color
        minimumSize = minSize
        preferredSize = minSize
        maximumSize = maxSize
        isFocusable = false
    }

    /**
     * Paints this `Filler`.  If this
     * `Filler` has a UI this method invokes super's
     * implementation, otherwise if this `Filler` is
     * opaque the `Graphics` is filled using the
     * background.
     *
     * @param g the `Graphics` to paint to
     * @throws NullPointerException if `g` is null
     */
    override fun paintComponent(g: Graphics) {
        g.color = background
        g.fillRoundRect(0, 2, width, height-4, 4, 4)
        g.dispose()
    }
}
