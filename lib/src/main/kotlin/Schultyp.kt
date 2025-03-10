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

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Beschreibt den Typ einer Schule
 */
@Suppress("unused")
enum class Schultyp(val jahre: Int) {
    @JsonProperty("Gymnasium") GYMNASIUM(12),
    @JsonProperty("Sekundarschule") SEKUNDARSCHULE(13),
    @JsonProperty("Berufsschule12") BERUFSSCHULE12(12),
    @JsonProperty("Berufsschule13") BERUFSSCHULE13(13);

    val ePhase: Int = jahre - 2
}
