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
import javax.swing.JComboBox
import kotlin.math.max

class FachComboBox(model: ComboBoxModel<Fach?>) : JComboBox<Fach?>(model) {
    override fun getSelectedIndex(): Int = max(super.getSelectedIndex(), 0)

    override fun getSelectedItem(): Fach? {
        return super.getSelectedItem() as Fach?
    }

    override fun getModel(): ComboBoxModel<Fach?> {
        return super.getModel() as ComboBoxModel<Fach?>
    }
}