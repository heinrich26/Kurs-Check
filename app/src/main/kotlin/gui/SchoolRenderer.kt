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

import com.kurswahlApp.data.Consts.COLOR_ON_BACKGROUND
import com.kurswahlApp.data.School
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class SchoolRenderer : JLabel(), ListCellRenderer<School> {
    init {
        isOpaque = true
    }

    companion object {
        val SEPARATOR = BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_ON_BACKGROUND)
    }

    override fun getListCellRendererComponent(
        list: JList<out School>,
        value: School,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        // einen Separator zwischen den Einträgen anzeigen
        border = if (list.model.size - 1 != index) SEPARATOR else null

        text = (value.name.wrapHtml("b", "font-size: 14px", "font-weight: 700") + "<br>" +
                value.adresse).wrapHtml("div", "margin: 0 4px").wrapHtml()

        if (isSelected) {
            background = list.selectionBackground
            foreground = list.selectionForeground
        } else {
            background = list.background
            foreground = list.foreground
        }

        return this
    }
}