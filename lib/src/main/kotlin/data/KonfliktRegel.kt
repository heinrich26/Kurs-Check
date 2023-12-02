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

/**
 * Legt fest, dass nur 1 der gegebenen Fächer gewählt werden kann
 */
@Suppress("unused")
class KonfliktRegel(private val wildcard: String, desc: String? = null, errorMsg: String? = null): Regel(desc, errorMsg) {
    override fun match(data: KurswahlData): Boolean {
        val wmoegls = data.kurse.mapNotNull { if (it.key in wildcardMembers) it.value else null }.toMutableList()
        while (wmoegls.size > 1) {
            val wmoegl1 = wmoegls.removeFirst()

            for (wmoegl2 in wmoegls)
                if (wmoegl1.intersects(wmoegl2)) return false
        }

//        for (fach1 in candidates) for (fach2 in candidates) {
//            if (fach1 == fach2) continue
//
//            if (fach1.value.intersects(fach2.value)) return true
//        }
        return true
    }

    private lateinit var wildcardMembers: List<Fach>

    override fun fillData(data: FachData) {
        wildcardMembers = data.wildcards[wildcard]!!
    }

    override fun toString(): String = toString(wildcard.named("wildcard"))
}