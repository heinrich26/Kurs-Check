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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class JsonVersion(val major: Int, val minor: Int) {
    @JsonValue
    override fun toString(): String = "$major.$minor"

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(version: String): JsonVersion {
            try {
                val (maj, min) = version
                    .split('.')
                    .takeIf { it.size == 2 }
                    ?.map(String::toInt) ?: throw IllegalArgumentException(INVALID_VERSION_ERROR_MSG)
                return JsonVersion(maj, min)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException(INVALID_VERSION_ERROR_MSG)
            }
        }

        private const val INVALID_VERSION_ERROR_MSG =
            "Ungültige Versionsbezeichnung! Eine Version besteht aus einer numerischen " +
                    "Haupt- und Subversion, getrennt durch einen „.“!"
    }
}