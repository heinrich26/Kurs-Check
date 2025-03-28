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

package com.kurswahlApp.github_status

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Klasse um den Status von Github zu ermitteln
 */
data class GithubStatus(
    val description: String,
    val indicator: Status
) {
    companion object {
        enum class Status {
            @JsonProperty("none")
            NONE,
            @JsonProperty("minor")
            MINOR,
            @JsonProperty("major")
            MAJOR,
            @JsonProperty("critical")
            CRITICAL
        }

        class PageStatus(
            val description: String,
            val indicator: Status
        )

        @JvmStatic
        @JsonCreator
        fun fromJson(@JsonProperty status: PageStatus): GithubStatus =
            GithubStatus(status.description, status.indicator)


        @JvmStatic
        fun get(): GithubStatus? =
            try {
                Scanner(
                    URI("https://www.githubstatus.com/api/v2/status.json").toURL().openStream(),
                    StandardCharsets.UTF_8.toString()
                ).use { scanner ->
                    scanner.useDelimiter("\\A")

                    if (scanner.hasNext())
                        jacksonObjectMapper().readValue<GithubStatus>(scanner.next())
                    else null
                }
            } catch (e: IOException) {
                null
            }
    }
}