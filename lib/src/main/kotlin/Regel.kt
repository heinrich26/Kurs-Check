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

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.kurswahlApp.data.Wahlmoeglichkeit.DURCHGEHEND


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

    /**
     * Erstellt ein Auswahl der Kurse mit dem gegebenen [RegelScope].
     * Prüfungsfächer werden in dieser Auswahl immer mit [Wahlmoeglichkeit.DURCHGEHEND] belegt, zur Nutzung
     * mit [Fach.blockAsPf] sollte eine [IfThenRegel] mit Scope nur in `regel1` und *ohne Scope* in `regel2`
     * verwendet werden.
     */
    protected fun getScope(scope: RegelScope?): (KurswahlData) -> Map<Fach, Wahlmoeglichkeit> =
        when (scope) {
            null -> { it -> it.kurse }
            RegelScope.LK1_2 -> { it -> it.lks.filterNotNull().associateWith { DURCHGEHEND } }
            RegelScope.PF1_4 -> { it -> it.pf1_4.filterNotNull().associateWith { DURCHGEHEND } }
            RegelScope.PF1_5 -> { it -> it.pfs.filterNotNull().associateWith { DURCHGEHEND } }
            RegelScope.PF3 -> { w -> w.pf3?.let { mapOf(it to DURCHGEHEND) } ?: emptyMap() }
            RegelScope.PF3_4 -> { w ->
                buildMap { w.pf3?.let { put(it, DURCHGEHEND) }; w.pf4?.let { put(it, DURCHGEHEND) } }
            }
            RegelScope.PF3_5 -> { it -> it.pf3_5.filterNotNull().associateWith { DURCHGEHEND } }
            RegelScope.PF4 -> { w -> w.pf4?.let { mapOf(it to DURCHGEHEND) } ?: emptyMap() }
            RegelScope.PF4_5 -> { w ->
                buildMap { w.pf4?.let { put(it, DURCHGEHEND) }; w.pf5?.let { put(it, DURCHGEHEND) } }
            }
            RegelScope.PF5 -> { w -> w.pf5?.let { mapOf(it to DURCHGEHEND) } ?: emptyMap() }
        }

    protected fun toString(vararg fields: String?) =
        fields.asList().plus(arrayOf("'$desc'".named("desc"), "'$errorMsg'".named("errorMsg"))).filterNotNull().joinToString(prefix = "${this::class.simpleName}(", postfix = ")")

    override fun toString(): String = toString(null)
}