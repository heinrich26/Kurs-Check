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

enum class RegelScope {
    @JsonProperty("1-2") LK1_2,
    @JsonProperty("1-4") PF1_4,
    @JsonProperty("1-5") PF1_5,
    @JsonProperty("3") PF3,
    @JsonProperty("3-4") PF3_4,
    @JsonProperty("3-5") PF3_5,
    @JsonProperty("4") PF4,
    @JsonProperty("4-5") PF4_5,
    @JsonProperty("5") PF5
}