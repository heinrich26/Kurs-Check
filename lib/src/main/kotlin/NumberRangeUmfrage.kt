/*
 * Copyright (c) 2025  Hendrik Horstmann
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

class NumberRangeUmfrage(title: String, desc: String?, min: Int, max: Int) : UmfrageBase<Int>(title, desc) {
    init {
        require(min < max) { "NumberRangeUmfrage: `min` muss kleiner sein als `max`" }
    }

    val range = IntRange(min, max)

    @Suppress("unused")
    constructor(title: String, desc: String, range: IntRange) : this(title, desc, range.first, range.last)

    override fun getCSVHeader(): List<String> = listOf("${title}_VALUE")

    override fun toCSVRow(data: Int): List<Int> = listOf(data)
}