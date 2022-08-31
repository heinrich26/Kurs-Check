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

import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed class Regel(var desc: String?, var errorMsg: String?) {
    /**
     * Stellt fest, ob die Regel erfüllt wird.
     * `true` steht für erfüllt
     */
    abstract fun match(data: KurswahlData): Boolean

    /**
     * Wird bei der Erstellung der Regel aufgerufen damit alle Regeln die benötigten Daten aus der FachData
     * erhalten können.
     */
    open fun fillData(data: FachData) {}
}