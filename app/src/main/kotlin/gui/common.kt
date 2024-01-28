/*
 * Copyright (c) 2023-2024  Hendrik Horstmann
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
import com.kurswahlApp.getResourceURL
import java.awt.Font
import java.awt.GraphicsEnvironment
import javax.swing.UIDefaults
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

fun prepareUI() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    arrayOf("Bold", "BoldItalic", "Italic", "Regular").forEach {
        ge.registerFont(
            Font.createFont(
                Font.TRUETYPE_FONT,
                getResourceURL("FiraSans-$it.ttf")!!.openStream()
            )
        )
    }

    val defaults = UIManager.getLookAndFeelDefaults()
    // Font Hack
    for ((key, value) in defaults) {
        if (key is String && key.endsWith(".font")) {
            // Hack für WindowsLookAndFeel
            if (value is UIDefaults.ActiveValue) {
                val val2 = value.createValue(defaults)
                if (val2 is FontUIResource) {
                    defaults[key] = FontUIResource(Consts.FONT_NAME, val2.style, 13)
                }
            } else if (value is FontUIResource) {
                // Hack für den Standard LookAndFeel
                defaults[key] = FontUIResource(Consts.FONT_NAME, value.style, 13)
            }
        }
    }
}