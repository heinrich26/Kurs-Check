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

import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed class UmfrageBase<T : Any>(val title: String, val desc: String?) {
    abstract fun getCSVHeader(): List<String>
    abstract fun toCSVRow(data: T): List<Any>

    /**
     * Die Methode wrapped die typisierte Methode `toCSVRow(data: T)` und gibt das Ergebnis zurück.
     * In der Anwendung ist sichergestellt, dass die Methode nur mit den passenden Daten, also zu der jeweiligen
     * Umfrage, aufgerufen wird.
     */
    @Suppress("UNCHECKED_CAST")
    @JvmName("toCSVRowRaw")
    fun toCSVRow(data: Any): List<Any> = toCSVRow(data as T)
}