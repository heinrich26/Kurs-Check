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

import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.Consts.FONT_NAME
import com.kurswahlApp.data.School
import java.awt.*
import javax.swing.*
import javax.swing.border.MatteBorder

class SchoolRenderer : JPanel(GridBagLayout()), ListCellRenderer<School> {
    private val title = JLabel()
    private val subtitle = JLabel()
    init {
        isOpaque = true
        title.font = TITLE_FONT
        title.foreground = Consts.COLOR_PRIMARY
        add(title, fill = GridBagConstraints.BOTH, weightx = 1.0, weighty = 1.0, margin = Insets(4))
        add(subtitle, row = 1, fill = GridBagConstraints.HORIZONTAL, margin = Insets(0,4,4,4))
    }

    companion object {
        val SEPARATOR: MatteBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE.darker())
        val TITLE_FONT = Font(FONT_NAME, Font.BOLD, 14)
    }

    override fun getListCellRendererComponent(
        list: JList<out School>,
        value: School,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        // einen Separator zwischen den Einträgen anzeigen
        border = SEPARATOR
        title.text = value.name.wrappable(166)
        subtitle.text = value.adresse.wrappable(166)

        background = if (isSelected) {
            Consts.COLOR_CONTROL
        } else {
            Consts.COLOR_BACKGROUND
        }

        return this
    }
}