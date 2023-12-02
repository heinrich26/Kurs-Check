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

import com.kurswahlApp.data.Fach
import java.awt.Component
import javax.swing.*
import kotlin.math.max

class FachComboBox(model: ComboBoxModel<Fach?>) : JComboBox<Fach?>(model) {
    override fun getSelectedIndex(): Int = max(super.getSelectedIndex(), 0)

    override fun getSelectedItem(): Fach? = super.getSelectedItem() as Fach?

    override fun getModel(): ComboBoxModel<Fach?> = super.getModel() as ComboBoxModel<Fach?>

    override fun getRenderer(): ListCellRenderer<in Fach?> = FachRenderer

    val selected
        get() = getSelectedItem()

    private object FachRenderer : DefaultListCellRenderer() {
        private fun readResolve(): Any = FachRenderer

        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component? = super.getListCellRendererComponent(
            list,
            if (value is Fach) value.name else "Ungesetzt",
            index,
            isSelected,
            cellHasFocus
        )
    }
}