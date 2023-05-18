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

import com.kurswahlApp.R
import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.KurswahlData
import com.kurswahlApp.data.Regel
import java.awt.*
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

class RegelLabel(private val regel: Regel) : JLabel(regel.desc?.wrappable(), validIcon, LEADING) {
    private var validText: String =
        (regel.desc ?: "Oh-oh, hier ist was schief gelaufen! (Dieser Regel fehlt eine Beschreibung)").wrappable()
    private var invalidText: String =
        (regel.errorMsg ?: "Oh-oh, hier ist was schief gelaufen! (Dieser Regel fehlt eine Fehlermeldung)").wrappable()

    /**
     * Überprüft die Regel, ändert ggf. das Aussehen das Labels und gibt das Ergebnis zurück
     */
    fun match(data: KurswahlData): Boolean = regel.match(data).also {
        setAppearance(it)
    }

    init {
        border = validBorder
        text = validText
        foreground = Consts.COLOR_VALID
    }

    private var apprearance = true

    private fun setAppearance(valid: Boolean) {
        if (apprearance != valid) {
            if (valid) {
                border = validBorder
                icon = validIcon
                text = validText
                foreground = Consts.COLOR_VALID
            } else {
                border = errorBorder
                icon = errorIcon
                text = invalidText
                foreground = Consts.COLOR_ERROR
            }
            apprearance = valid
        }
    }

    companion object {
        val validIcon = ShapeIcon(R.task_check, 24)
        val errorIcon = ShapeIcon(R.error, 24)
        private val validBorder = EmptyBorder(2, 5, 2, 5)
        private val errorBorder = FilledRoundedBorder(Insets(2), Consts.COLOR_ERROR)
    }
}