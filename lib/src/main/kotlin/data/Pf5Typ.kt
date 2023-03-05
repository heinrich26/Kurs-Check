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

package com.kurswahlApp.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.kurswahlApp.data.Consts.PF_5_VAL_BLL
import com.kurswahlApp.data.Consts.PF_5_VAL_PRAES
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

enum class Pf5Typ(val repr: String, val lusdId: String) {
    @JsonProperty("schriftl") SCHRIFTLICH("schriftlich", PF_5_VAL_BLL),
    @JsonProperty("praes") PRAESENTATION("Präsentation", PF_5_VAL_PRAES),
    @JsonProperty("wettbewerb") WETTBEWERB("Wettbewerb", PF_5_VAL_BLL);

    object Renderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component? = super.getListCellRendererComponent(
            list,
            if (value is Pf5Typ) value.repr else "Ungesetzt",
            index,
            isSelected,
            cellHasFocus
        )
    }

    companion object {
        @JvmStatic
        fun byLusdId(id: String): Pf5Typ = when (id) {
            PF_5_VAL_BLL -> SCHRIFTLICH
            PF_5_VAL_PRAES -> PRAESENTATION
            else -> PRAESENTATION
        }
    }
}