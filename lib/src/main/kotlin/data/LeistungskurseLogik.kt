/*
 * Copyright (c) 2023  Hendrik Horstmann
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

import com.kurswahlApp.data.Wahlzeile.Companion.isWildcard

class LeistungskurseLogik(
    private val fachData: FachData,
    private val wahlData: KurswahlData,
) {
    // Eine Fremdsprache, die erst in der Jahrgangsstufe 10 oder in der Einführungsphase begonnen wurde,
    // darf nur als 3. oder 4. Prüfungsfach oder als Referenzfach der 5. PK gewählt werden.
    private val fs =
        wahlData.fremdsprachen.mapNotNull { (fach, jahr) -> fach.takeIf { (jahr < fachData.schultyp.ePhase) } }
    private val wpfs = wahlData.wpfs

    val lk1Moeglichkeiten = fachData.lk1Moeglichkeiten.filter {
        (if (it.isFremdsprache) it in fs
        else !it.brauchtWPF || (wpfs != null && it in wpfs)) && it.checkKlasse(wahlData.klasse)
    }

    fun getLk2Moeglichkeiten(first: Fach?) =
        buildSet {
            for ((lk1, lk2) in fachData.wahlzeilen.values) {
                if (lk1.isWildcard && first != null
                    && first.kuerzel in fachData.wzWildcards[lk1]!!
                    || lk1 == first?.kuerzel) {
                    if (lk2.isWildcard) addAll(fachData.wzWildcards[lk2]!!)
                    else add(lk2)
                }
            }
        }.mapNotNull { k -> fachData.faecherMap[k]!!.takeIf { it.isLk && it != first && checkFach(it) } }

    /**
     * Prüft ob das Fach in Hinblick auf Fremdsprachen und Wahlpflichtfächer als LK gewält werden kann.
     */
    private fun checkFach(fach: Fach) =
            /* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 */
            (if (fach.isFremdsprache) fach in fs
            /* Hat keine WPF or Fach ist weder 1./2. WPF */
            else fach.checkWpf(wpfs)) && fach.checkKlasse(wahlData.klasse)

    fun validate(lk1: Fach?, lk2: Fach?) = lk1 != null && lk2 != null

    fun save(lk1: Fach, lk2: Fach) = wahlData.updateLKs(lk1, lk2)
}