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

import com.kurswahlApp.data.Fach
import javax.swing.ComboBoxModel
import javax.swing.event.ListDataListener

class ExclusiveComboBoxModel(var data: List<Fach>, private val nachfolger: FachComboBox? = null) :
    ComboBoxModel<Fach?> {

    override fun getSize(): Int = data.size + 1

    private val listeners = mutableListOf<ListDataListener>()

    private var selectedItem: Fach? = null

    override fun getElementAt(index: Int): Fach? = if (index == 0) null else data[index - 1]

    override fun addListDataListener(l: ListDataListener?) {
        if (l != null) listeners.add(l)
    }

    override fun removeListDataListener(l: ListDataListener?) {
        if (l != null) listeners.remove(l)
    }

    override fun setSelectedItem(anItem: Any?) {
        selectedItem = anItem as Fach?
        if (nachfolger != null)
            if (selectedItem != null) {
                (nachfolger.model as ExclusiveComboBoxModel).data = data.minus(selectedItem!!)
                if (nachfolger.selectedItem == selectedItem) nachfolger.selectedItem = null
            } else {
                (nachfolger.model as ExclusiveComboBoxModel).data = data
                nachfolger.selectedItem = null
            }
    }

    override fun getSelectedItem(): Fach? = selectedItem
}