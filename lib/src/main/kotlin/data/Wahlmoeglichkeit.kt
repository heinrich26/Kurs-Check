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

@Suppress("unused")
enum class Wahlmoeglichkeit(val n: Int, val bools: List<Boolean>) {
    @JsonProperty("1-2") ERSTES_ZWEITES(2, listOf(true, true, false, false)),
    @JsonProperty("1-3") ERSTES_DRITTES(3, listOf(true, true, true, false)),
    @JsonProperty("2-4") ZWEITES_VIERTES(3, listOf(false, true, true, true)),
    @JsonProperty("3-4") DRITTES_VIERTES(2, listOf(false, false, true, true)),
    @JsonProperty("1-4") DURCHGEHEND(4, listOf(true, true, true, true));

    operator fun contains(wmoegl: Wahlmoeglichkeit): Boolean {
        /*
         * alternativer Ansatz mit vararg und contains, ist aber vermutlich langsamer
         * -> im Constructor: (...:..., vararg contains: Wahlmoeglichkeit)
         *    ERSTES_DRITTES(..., ERSTES_ZWEITES, ERSTES_DRITTES)
         */
        return when (this) {
            ERSTES_ZWEITES -> wmoegl == ERSTES_ZWEITES
            ERSTES_DRITTES -> wmoegl == ERSTES_ZWEITES || wmoegl == ERSTES_DRITTES
            ZWEITES_VIERTES -> wmoegl == DRITTES_VIERTES || wmoegl == ZWEITES_VIERTES
            DRITTES_VIERTES -> wmoegl == DRITTES_VIERTES
            else /* DURCHGEGEHND */ -> true
        }
    }

    /**
     * Gibt an ob sich die Semester der beiden [Wahlmoeglichkeit]en überschneiden
     */
    fun intersects(other: Wahlmoeglichkeit): Boolean {
        return when (this) {
            ERSTES_ZWEITES -> other != DRITTES_VIERTES
            ERSTES_DRITTES -> true
            ZWEITES_VIERTES -> other != ERSTES_ZWEITES
            DRITTES_VIERTES -> true
            DURCHGEHEND -> true
        }
    }

    companion object {
        /**
         * Gibt die [Wahlmoeglichkeit], welche der gegebenen Liste aus 4 Bools entspricht zurück
         */
        fun fromBools(bools: List<Boolean>): Wahlmoeglichkeit? = when (bools) {
            ERSTES_ZWEITES.bools -> ERSTES_ZWEITES
            ERSTES_DRITTES.bools -> ERSTES_DRITTES
            ZWEITES_VIERTES.bools -> ZWEITES_VIERTES
            DRITTES_VIERTES.bools -> DRITTES_VIERTES
            DURCHGEHEND.bools -> DURCHGEHEND
            else -> null
        }


        val UNGEWAEHLT_BOOLS = listOf(false, false, false, false)
    }

}