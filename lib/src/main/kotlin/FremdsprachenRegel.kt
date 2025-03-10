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

/**
 * Regel die erfüllt ist, eine Fremdsprache in JG 10 oder der E-Phase begonnen wurde.
 * Mit dem Parameter [ohneFs2] kann die Regel nur auf FS 3/4 begrenzt werden.
 *
 * **Wichtig:** Nur für die Verwendung mit [IfThenRegel] gedacht, um die Belegungsverpflichtung
 * des *künstlerischen Fachs* aufzuheben oder die Belegung einer weiteren Fremdsprache zu erzwingen.
 */
@Suppress("unused")
class FremdsprachenRegel(private val ohneFs2: Boolean = false, desc: String?, errorMsg: String?) :
    Regel(desc, errorMsg) {

    override fun match(data: KurswahlData): Boolean =
        data.fremdsprachen
            .drop(if (ohneFs2) 2 else 1)
            .any { (fach, jahr) -> (jahr >= 10) && fach in data.kurse }
}