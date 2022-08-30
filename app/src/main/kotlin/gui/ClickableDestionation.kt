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

import com.kurswahlApp.data.Consts
import java.awt.Dimension
import javax.swing.JComponent

@Suppress("unused", "unused", "unused")
open class ClickableDestionation(
    defaultSelected: Boolean = false,
    defaultEnabled: Boolean = true,
    clickEvent: () -> Unit
) : JComponent() {
    var hasFocus = false
        set(value) {
            field = value
            repaint()
        }

    var isSelected = defaultSelected
        set(value) {
            field = value
            repaint()
        }

    init {
        Dimension(Consts.SIDEBAR_SIZE, Consts.SIDEBAR_SIZE).let {
            minimumSize = it
            preferredSize = it
        }

        isEnabled = defaultEnabled

        this.addMouseListener(
            onClick = { if (isEnabled && !isSelected) clickEvent() },
            onEnter = { hasFocus = isEnabled },
            onExit = { hasFocus = false }
        )
    }
}
