/*
 * Copyright (c) 2023  Hendrik Horstmann
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
 * Regel die erfüllt ist, sofern [regel] nicht erfüllt wird. (inverse)
 *
 * **Wichtig:** es ist nicht gesagt, dass diese Regel besonders gut mit den (komplexeren) anderen funktioniert.
 */
@Suppress("unused")
class NotRegel(private val regel: Regel, desc: String?, errorMsg: String?) : Regel(desc, errorMsg) {
    override fun match(data: KurswahlData): Boolean = !regel.match(data)

    override fun fillData(data: FachData) = regel.fillData(data)

    override fun toString(): String = toString(regel.named("regel"))
}