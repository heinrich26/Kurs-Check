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

package gui

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kurswahlApp.data.FachData
import com.kurswahlApp.getResource
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.exists


object SchoolConfig {
    const val CONFIG_SERVER_URL = "https://raw.githubusercontent.com/heinrich26/Kurs-Check/data/"
    val CONFIG_FILE_URL = URL(CONFIG_SERVER_URL + "per-school-settings.json")
    const val CONFIG_FOLDER_URL = CONFIG_SERVER_URL + "schools/"

    val LOCAL_CONFIG_DIR = System.getProperty("os.name").lowercase().let {
        when {
            "win" in it -> {
                System.getenv("AppData")
            }
            "nix" in it || "nux" in it || "aix" in it -> {
                System.getProperty("user.home")
            }
            "mac" in it -> {
                System.getProperty("user.home") + "/Library/Preferences"
            }
            else -> System.getProperty("user.home")
        }!! + "/.kurs-check"
    }

    val LOCAL_SCHOOLS_DIR = "$LOCAL_CONFIG_DIR/schools/"
    val LOCAL_MAIN_CONFIG = "$LOCAL_CONFIG_DIR/per-school-settings.json"
    val LOCAL_LAST_SCHOOL_PROPS = "$LOCAL_CONFIG_DIR/last.properties"


    init {
        Files.createDirectories(Paths.get(LOCAL_SCHOOLS_DIR))
    }

    lateinit var schools: List<School>

    fun updateConfig() {
        val mapper = jacksonObjectMapper()
        mapper.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
        schools = try {
            mapper.readValue(fetchConfig())
        } catch (e: IOException) {
            mapper.readValue(loadConfig())
        }
    }

    /** Loads the Config from the Server */
    fun fetchConfig(): String {
        Scanner(
            CONFIG_FILE_URL.openStream(),
            StandardCharsets.UTF_8.toString()
        ).use { scanner ->
            scanner.useDelimiter("\\A")

            if (scanner.hasNext())
                return scanner.next().also {
                    suspend { runCatching { Files.writeString(Paths.get(LOCAL_MAIN_CONFIG), it) } }
                }
            else throw IOException()
        }
    }

    /** Versucht die Konfiguration für die angeforderte Schule vom Server zu laden */
    fun fetchSchool(schoolKey: String): String? {
        Scanner(
            URL(CONFIG_FOLDER_URL + schoolKey).openStream(),
            StandardCharsets.UTF_8.toString()
        ).use { scanner ->
            scanner.useDelimiter("\\A")

            return if (scanner.hasNext())
                scanner.next().also {
                    suspend {
                        runCatching { Files.writeString(Paths.get(LOCAL_SCHOOLS_DIR + schoolKey), it) }
                    }
                }
            else null
        }
    }

    fun loadSchool(schoolKey: String): String? {
        with(Paths.get(LOCAL_SCHOOLS_DIR, schoolKey)) {
            return if (this.exists()) Files.readString(this) else null
        }
    }

    fun getSchool(schulId: String): FachData? {
        val mapper = jacksonObjectMapper()
        mapper.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)

        return try {
            fetchSchool(schulId) ?: loadSchool(schulId)
        } catch (e: IOException) {
            println("failed")
            loadSchool(schulId)
        }?.let { mapper.readValue(it, FachData::class.java) }
    }

    /**
     * Lädt die lokale Konfigurationsdatei, wenn z.B. kein Internet vorhanden ist
     * Ist keine lokale Datei vorhanden wird die Fallback-Datei aus der .jar verwendet!
     */
    fun loadConfig(): String = with(Paths.get(LOCAL_MAIN_CONFIG)) {
        if (this.exists()) Files.readString(this)
        else getResource("schools-data/per-school-settings.json")!!
    }

    /**
     * Property-Name der zuletzt verwendeten Schule in der Properties-Datei
     */
    private const val LAST_KEY = "last"

    /**
     * Läd die zuletzt genutzte Schule aus der Konfigurations-Datei
     */
    fun loadLastSchool(): String? {
        val schoolProps = Properties()
        try {
            schoolProps.load(FileInputStream(LOCAL_LAST_SCHOOL_PROPS))
        } catch (e: IOException) {
            return null
        }
        return schoolProps.getProperty(LAST_KEY)
    }

    /**
     * Speichert die zuletzt verwendete Schule asynchron im Config Ordner
     * @param schoolKey ID der Schule
     */
    fun writeLastSchool(schoolKey: String) {
        suspend {
            with(Properties()) {
                setProperty(LAST_KEY, schoolKey)
                runCatching {
                    store(FileWriter(LOCAL_LAST_SCHOOL_PROPS), null)
                }

            }
        }
    }

}