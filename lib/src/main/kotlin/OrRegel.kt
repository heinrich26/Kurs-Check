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
/**
 * Regel, die erfüllt wird wenn mindestens eine der beiden assoziierten Regeln erfüllt wird.
 */
class OrRegel(private val regel1: Regel, private val regel2: Regel, desc: String? = null, errorMsg: String? = null) : Regel(desc, errorMsg) {
    override fun match(data: KurswahlData): Boolean = regel1.match(data) || regel2.match(data)

    override fun fillData(data: FachData) {
        regel1.fillData(data)
        regel2.fillData(data)
    }

    override fun toString(): String = toString(regel1.named("regel1"), regel2.named("regel2"))
}