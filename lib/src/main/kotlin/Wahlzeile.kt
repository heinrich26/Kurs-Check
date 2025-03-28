/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
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
data class Wahlzeile(
    val lk1: String,
    val lk2: String,
    val pf3: String,
    val pf4: String,
    val pf5: String,
    val linien: WahlzeileLinientyp) {

    fun toList(): List<String> = listOf(lk1, lk2, pf3, pf4, pf5)

    companion object {
        val String.isWildcard: Boolean
            get() = this.startsWith('$')

        val String.isAny: Boolean
            get() = this == "*"
    }
}

