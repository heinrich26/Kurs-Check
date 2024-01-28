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

package com.kurswahlApp.data

@Suppress("unused")
class KuerzelRegel(
    private val kuerzel: String,
    private val anzahl: Int,
    private val wann: Wahlmoeglichkeit? = null,
    private val scope: RegelScope? = null,
    desc: String? = null,
    errorMsg: String? = null
) : Regel(desc, errorMsg) {

    private val predicate: (Wahlmoeglichkeit) -> Boolean =
        if (wann == null) { it -> (it.n >= anzahl) } else { it -> (it.n >= anzahl && wann in it) }

    private val dataScope: (KurswahlData) -> Map<Fach, Wahlmoeglichkeit> = super.getScope(scope)

    private lateinit var target: Fach

    override fun match(data: KurswahlData): Boolean {
        for ((fach, wmoegl) in dataScope(data)) {
            // Gucken ob das Kürzel passt
            if (fach != target) continue
            // Checken ob die Wahlmoeglichkeit passt
            if (predicate(wmoegl)) return true

        }

        return false
    }

    override fun fillData(data: FachData) {
        target = data.faecher.find { it.kuerzel == kuerzel }!!
    }

    override fun toString(): String =
        toString(kuerzel.named("kuerzel"), anzahl.named("anzahl"), wann.named("wann"), scope.named("scope"))
}