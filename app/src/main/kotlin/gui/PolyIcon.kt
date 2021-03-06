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

package gui

import com.kurswahlApp.data.Consts.COLOR_CONTROL
import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND
import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND_DISABLED
import com.kurswahlApp.data.Consts.COLOR_PRIMARY
import com.kurswahlApp.data.Consts.RENDERING_HINTS
import com.kurswahlApp.data.Consts.SIDEBAR_SIZE
import java.awt.BasicStroke
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform

class PolyIcon(poly: Shape, defaultSelected: Boolean, clickEvent: () -> Unit) :
    ClickableDestionation(defaultSelected,  clickEvent = clickEvent) {
    companion object {
        val transform1 = AffineTransform(1.0, 0.0, 0.0, 1.0, -12.0, -12.0)
        val transform2 = (SIDEBAR_SIZE / 36.0).let { AffineTransform(it, 0.0, 0.0, it, 0.0, 0.0) }
        val transform3 = (SIDEBAR_SIZE / 2.0).let { AffineTransform(1.0, 0.0, 0.0, 1.0, it, it) }
    }

    private val shape =
        transform3.createTransformedShape(transform2.createTransformedShape(transform1.createTransformedShape(poly)))

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2D = g as Graphics2D

        g2D.setRenderingHints(RENDERING_HINTS)

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