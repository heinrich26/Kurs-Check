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

import com.kurswahlApp.data.Consts
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import javax.swing.JComponent

class HoverableIconButton(shape: Shape,
                          iconSize: Int = 24,
                          targetSize: Int? = null,
                          val color: Color = Consts.COLOR_PRIMARY,
                          val selectedColor: Color = Consts.COLOR_PRIMARY,
                          val hoverColor: Color = Consts.COLOR_CONTROL,
                          val disabledColor: Color = Consts.COLOR_ON_BACKGROUND_DISABLED,
                          defaultSelected: Boolean = false,
                          defaultEnabled: Boolean = true,
                          clickEvent: (enabled: Boolean) -> Unit,) : JComponent() {
    private var hasFocus = false
        set(value) {
            field = value
            repaint()
        }

    var isSelected = defaultSelected
        set(value) {
            field = value
            repaint()
        }

    private val realSize = targetSize ?: iconSize
    private val shape = (realSize / iconSize.toDouble()).let { AffineTransform.getScaleInstance(it, it).createTransformedShape(shape) }
    init {
        (realSize by realSize).let {
            minimumSize = it
            preferredSize = it
        }

        isEnabled = defaultEnabled

        this.addMouseListener(
            onClick = { if (!isSelected) clickEvent(isEnabled) },
            onEnter = { hasFocus = isEnabled },
            onExit = { hasFocus = false }
        )
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2D = g as Graphics2D

        g2D.setRenderingHints(Consts.RENDERING_HINTS)

        if (hasFocus) {
            g2D.color = hoverColor
            g2D.fillOval(0, 0, width, height)
        }

        g2D.color = if (isSelected) selectedColor
        else if (isEnabled) color
        else disabledColor

        g2D.fill(shape)

        g2D.dispose()
    }
}