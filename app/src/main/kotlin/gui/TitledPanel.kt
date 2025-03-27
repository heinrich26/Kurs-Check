/*
 * Copyright (c) 2025  Hendrik Horstmann
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

import com.kurswahlApp.gui.RoundedBorder
import java.awt.FlowLayout
import java.awt.LayoutManager
import javax.swing.JPanel
import javax.swing.border.TitledBorder

open class TitledPanel(
    title: String,
    radius: Int = 8,
    layout: LayoutManager = FlowLayout(),
    isDoubleBuffered: Boolean = true
) : JPanel(layout, isDoubleBuffered) {
    init {
        this.border = TitledBorder(RoundedBorder(radius), title)
    }
}