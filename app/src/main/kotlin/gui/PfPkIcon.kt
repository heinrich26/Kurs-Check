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

import com.kurswahlApp.data.Consts.COLOR_CONTROL
import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND
import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND_DISABLED
import com.kurswahlApp.data.Consts.COLOR_PRIMARY
import com.kurswahlApp.data.Consts.FONT_NAME
import com.kurswahlApp.data.Consts.RENDERING_HINTS
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D

class PfPkIcon(clickEvent: () -> Unit) : ClickableDestionation(clickEvent = clickEvent) {

    companion object {
        @JvmStatic
        val bigFont = Font(FONT_NAME, Font.BOLD, 18)

        @JvmStatic
        val smallFont = Font(FONT_NAME, Font.BOLD, 15)
    }

    private val pfLen: Int
    private val pkLen: Int

    init {
        getFontMetrics(bigFont).let {
            pfLen = it.stringWidth("PF")
            pkLen = it.stringWidth("5. PK")
        }
    }

    override fun paintComponent(g: Graphics) {
        val g2D = g as Graphics2D
        g2D.setRenderingHints(RENDERING_HINTS)

        if (hasFocus) {
            g2D.color = COLOR_CONTROL
            g2D.fillOval(5, 5, width - 10, height - 10)
        }

        // Hintergrund nur malen, wenn Ausgewählt

        g2D.color = if (isSelected) {
            COLOR_PRIMARY
        } else if (isEnabled) COLOR_ON_BACKGROUND
        else COLOR_ON_BACKGROUND_DISABLED

        g2D.font = bigFont

        g2D.drawString("PF", 10, 36)
        g2D.drawString("5. PK", 18, 54)
        g2D.font = smallFont
        g2D.drawString("s", 10 + pfLen, 36)

        g2D.dispose()
    }
}