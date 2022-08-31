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

import com.kurswahlApp.createImageIcon
import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.KurswahlData
import com.kurswahlApp.data.Regel
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

class RegelLabel(private val regel: Regel) : JLabel(regel.desc?.wrappable() ?: "", validIcon, LEADING) {
    private var validText: String = regel.desc?.wrappable() ?: ""
    private var invalidText: String = regel.errorMsg?.wrappable() ?: ""

    /**
     * Aktualisiert den Text, falls sich die Texte der Regel geändert haben
     */
    private fun updateText() {
        validText = regel.desc?.wrappable() ?: ""
        invalidText = regel.errorMsg?.wrappable() ?: ""
    }

    /**
     * Überprüft die Regel, ändert ggf. das Aussehen das Labels und gibt das Ergebnis zurück
     */
    fun match(data: KurswahlData, textUpdate: Boolean = false): Boolean = regel.match(data).also {
        if (textUpdate) updateText()
        setAppearance(it)
    }


    init {
        border = EmptyBorder(2, 4, 2, 0)
        text = validText
        foreground = Consts.COLOR_VALID
    }

    private var apprearance = true

    fun setAppearance(valid: Boolean) {
        if (apprearance != valid) {
            if (valid) {
                icon = validIcon
                text = validText
                foreground = Consts.COLOR_VALID
            } else {
                text = invalidText
                foreground = Consts.COLOR_ERROR
                icon = errorIcon
            }
            apprearance = valid
        }
    }

    companion object {
        private val validIcon = createImageIcon("icons/check.png")
        private val errorIcon = createImageIcon("icons/cross.png")
    }
}