/*
 * Copyright (c) 2023-2025  Hendrik Horstmann
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

import com.kurswahlApp.data.Wahlzeile.Companion.isAny
import com.kurswahlApp.data.Wahlzeile.Companion.isWildcard
import kotlin.reflect.KCallable

@Suppress("PropertyName")
class PruefungsfaecherLogik(
    private val fachData: FachData,
    private val wahlData: KurswahlData,
) {

    //region Referenzen zu gettern von Eingabewerten
    lateinit var _pf3: KCallable<Fach?>
    lateinit var _pf4: KCallable<Fach?>
    lateinit var _pf5: KCallable<Fach?>
    lateinit var _pf5Typ: KCallable<Pf5Typ?>

    private val pf3
        get() = _pf3.call()
    private val pf4
        get() = _pf4.call()
    private val pf5
        get() = _pf5.call()
    private val pf5Typ
        get() = _pf5Typ.call()
    //endregion

    private val userFs = wahlData.fremdsprachen.map { it.first }
    private val userWpfs = wahlData.wpfs
    private val filteredZeilen = filterWahlzeilen()
    private val filteredFaecherPf5: Map<String, Fach>

    init {
        @Suppress("ConvertArgumentToSet")
        val conflicts: List<String> = wahlData.lks.mapNotNull { it?.kuerzel }.let { lks ->
            fachData.conflictGroups.filter { group -> lks.any { it in group } }.flatten() - lks
        }
        filteredFaecherPf5 = fachData.faecherMap.filterValues {
            /* Nach VO-GO Berlin - § 23 Wahl der Prüfungsfächer Nr.8 muss das Fach für die 5. PK lediglich 4
            Semester belegt werden. */
            // ist als Kurs wählbar
            it.isPf && it.isKurs &&
                    // Fach wird als Grundkurs angeboten oder als LK belegt
                    (it.isGk || (it.isLk && it in wahlData.lks)) &&
                    // Darf mit der besuchten Klasse gewählt werden
                    it.checkKlasse(wahlData.klasse) &&
                    // Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1
                    /* TODO die VO-GO verbietet nicht, eine nichtgewählte Fremdsprache zur 5. PK zu wählen,
                        wir setzen dies hier jedoch vorraus, weil alles andere Sinnlos scheint. */
                    // Schule hat keine strikten WPFs oder Fach ist kein WPF bzw. Schüler hatte es in Sek 1 oder
                    (if (it.isFremdsprache) it in userFs else !fachData.strikteWPFs || it.checkWpf(userWpfs)) &&
                    // KonfliktRegel beachten
                    it.kuerzel !in conflicts
        }
    }
    // Fächer für PF 3 & 4
    private val filteredFaecher = filteredFaecherPf5.filterValues {
        // Fach kann als Grundkurs gewählt werden und ist Prüfungsfach
        it.isPf && it.isGk &&
                // Fach wurde nicht als LK gewählt
                it !in wahlData.lks &&
                // Braucht kein WPF oder Fach war WPF in Sek 1
                it.checkWpf(userWpfs)
    }

    private val zeilenFuerFuenfte: MutableSet<Pair<Int, Wahlzeile>> = mutableSetOf()

    fun pf3Faecher(): Collection<Fach> {
        val kuerzel = mutableSetOf<String>()
        val predicate: (Fach) -> Boolean = { f1 ->
            // VOGO §23, Nr.7-3: Fremdsprachen aus der E-Phase dürfen nur als 4./5. PF gewählt werden.
            f1.nurPf4_5 || (f1.isFremdsprache
                    && wahlData.fremdsprachen.any { (f2, yr) -> f1 == f2 && yr >= fachData.schultyp.ePhase })
        }

        fun beliebig() = filteredFaecher.mapNotNull { it.value.takeUnless(predicate) }

        for ((_, _, pf3, pf4, pf5, linien) in filteredZeilen.values) {
            if (pf3.isAny) return beliebig()

            if (pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf3]!!)
            else kuerzel.add(pf3)

            if (linien != WahlzeileLinientyp.DURCHGEZOGEN) {
                if (pf4.isAny) return beliebig()

                if (pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf4]!!)
                else kuerzel.add(pf4)

                if (linien != WahlzeileLinientyp.KEINE_DURCHGEZOGEN) {
                    if (pf5.isAny) return beliebig()

                    if (pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf5]!!)
                    else kuerzel.add(pf5)
                }
            }
        }

        return kuerzel.mapNotNull { filteredFaecher[it]?.takeUnless(predicate) }
    }


    private val pf4ConflictGroups = fachData.conflictGroups + listOf(fachData.pf3_4AusschlussFaecher.toList())
    fun pf4Faecher(): List<Fach> {
        val selectedPf3 = pf3 ?: return emptyList()
        val pf3Groups = fachData.wzWildcardMapping[selectedPf3]!!

        val conflicts = pf4ConflictGroups.filter { selectedPf3.kuerzel in it }.flatten().toSet()

        fun beliebig(): List<Fach> = filteredFaecher.values.filter { it != selectedPf3 && it.kuerzel !in conflicts }

        val kuerzel = mutableSetOf<String>()
        for ((_, _, pf3, pf4, pf5, linien) in filteredZeilen.values) {
            if (pf3 in pf3Groups) {
                if (pf4.isAny) return beliebig()

                if (pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf4]!!)
                else kuerzel.add(pf4)

                if (linien == WahlzeileLinientyp.GESTRICHELT || linien == WahlzeileLinientyp.KEINE) {
                    if (pf5.isAny) return beliebig()

                    if (pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf5]!!)
                    else kuerzel.add(pf5)
                }
            }

            if (linien != WahlzeileLinientyp.DURCHGEZOGEN) {
                if (pf4 in pf3Groups) {
                    if (pf3.isAny) return beliebig()

                    if (pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf3]!!)
                    else kuerzel.add(pf3)

                    if (linien == WahlzeileLinientyp.KEINE) {
                        if (pf5.isWildcard) return beliebig()

                        if (pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf5]!!)
                        else kuerzel.add(pf5)
                    }
                }

                if (pf5 in pf3Groups) {
                    if (pf3.isAny) return beliebig()

                    if (linien == WahlzeileLinientyp.KEINE) {
                        if (pf4.isAny) return beliebig()

                        if (pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf4]!!)
                        else kuerzel.add(pf4)
                    }

                    if (pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf3]!!)
                    else kuerzel.add(pf3)
                }
            }
        }

        kuerzel.removeAll(conflicts)
        kuerzel.remove(selectedPf3.kuerzel) // Prüfungsfach 3 nicht 2x wählen
        return kuerzel.mapNotNull(filteredFaecher::get)
    }

    @Suppress("ConvertArgumentToSet")
    fun pf5Faecher(): List<Fach> {
        val selectedPf3 = pf3 ?: return emptyList()
        val selectedPf4 = pf4 ?: return emptyList()

        val pf3Groups = fachData.wzWildcardMapping[selectedPf3]!!
        val pf4Groups = fachData.wzWildcardMapping[selectedPf4]!!


        // bei Präsentation bereits gewählte entfernen!
        fun beliebig(): List<Fach> =
            (if (pf5Typ == Pf5Typ.PRAESENTATION)
                filteredFaecherPf5.values - arrayOf(wahlData.lk1!!, wahlData.lk2!!, selectedPf3, selectedPf4)
            else filteredFaecherPf5.values.toList()).removeConflicts2(selectedPf3, selectedPf4)

        val kuerzel = mutableSetOf<String>()

        zeilenFuerFuenfte.clear()

        for (wz in filteredZeilen.values) {
            // Fall: alle durchgezogen; keine Vertauschung
            if (wz.pf3 in pf3Groups && wz.pf4 in pf4Groups) {
                zeilenFuerFuenfte.add(5 to wz)
                if (wz.pf5.isAny) return beliebig()

                if (wz.pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                else kuerzel.add(wz.pf5)
            }

            if (wz.linien != WahlzeileLinientyp.DURCHGEZOGEN) {
                // Fall: keine|durchgezogen; 3./4. getauscht
                if (wz.pf4 in pf3Groups && wz.pf3 in pf4Groups) {
                    zeilenFuerFuenfte.add(5 to wz)
                    if (wz.pf5.isAny) return beliebig()

                    if (wz.pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                    else kuerzel.add(wz.pf5)
                }
                // Fall: gestrichelte Linien oder keine Linien
                //       3./5. oder 4./5. getauscht
                if (wz.linien != WahlzeileLinientyp.KEINE_DURCHGEZOGEN) {
                    if ((wz.pf3 in pf3Groups && wz.pf5 in pf4Groups) || (wz.pf3 in pf4Groups && wz.pf5 in pf3Groups)) {
                        zeilenFuerFuenfte.add(4 to wz)
                        if (wz.pf4.isAny) return beliebig()

                        if (wz.pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                        else kuerzel.add(wz.pf4)
                    }
                    if ((wz.pf4 in pf3Groups && wz.pf5 in pf4Groups) || (wz.pf4 in pf4Groups && wz.pf5 in pf3Groups)) {
                        zeilenFuerFuenfte.add(3 to wz)
                        if (wz.pf3.isAny) return beliebig()

                        if (wz.pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[wz.pf3]!!)
                        else kuerzel.add(wz.pf3)
                    }
                }
            }
        }

        // bei Präsentation bereits gewählte entfernen!
        if (pf5Typ == Pf5Typ.PRAESENTATION) {
            kuerzel.removeAll(
                listOf(wahlData.lk1!!.kuerzel, wahlData.lk2!!.kuerzel, selectedPf3.kuerzel, selectedPf4.kuerzel)
            )
        }

        return kuerzel.removeConflicts(selectedPf3, selectedPf4).mapNotNull(filteredFaecher::get)
    }

    /**
     * Gibt alle möglichen Wahlzeilen für die gegebenen LKs zurück
     */
    private fun filterWahlzeilen(): Map<Int, Wahlzeile> {
        val lk1Wildcard = fachData.wzWildcardMapping.getValue(wahlData.lk1!!)
        val lk2Wildcard = fachData.wzWildcardMapping.getValue(wahlData.lk2!!)

        return fachData.wahlzeilen.filterValues {
            (it.lk1 in lk1Wildcard && it.lk2 in lk2Wildcard) || (it.lk2 in lk1Wildcard && it.lk1 in lk2Wildcard)
        }
    }

    private fun getWahlzeile(): WahlzeileNummer {
        val selectedPf5 = pf5
        val pf5Groups = fachData.wzWildcardMapping[selectedPf5]!!
        var n: Int = -1
        for ((field, zeile) in zeilenFuerFuenfte) {
            val fieldVal = when (field) {
                3 -> zeile.pf3
                4 -> zeile.pf4
                else /* 5 */ -> zeile.pf5
            }
            if (fieldVal in pf5Groups) {
                n = filteredZeilen.firstNotNullOf { (k, v) -> k.takeIf { v == zeile } }
                break
            }
        }

        return WahlzeileNummer(n)
    }

    fun validate(): Boolean = (pf3 != null && pf4 != null && pf5 != null)

    /**
     * Soll nur ausgeführt werden, wenn [validate] `true` ergibt.
     */
    fun save(): KurswahlData {
        assert(validate())

        return wahlData.updatePFs(
            pf3 = pf3!!,
            pf4 = pf4!!,
            pf5 = pf5!!,
            pf5Typ = pf5Typ!!,
            wahlzeile = getWahlzeile()
        )
    }

    @Suppress("ConvertArgumentToSet")
    private fun Iterable<String>.removeConflicts(fach: Fach, fach2: Fach): List<String> =
        this - fachData.conflictGroups.filter { fach.kuerzel in it || fach2.kuerzel in it }.flatten()

    private fun Iterable<Fach>.removeConflicts2(fach: Fach, fach2: Fach): List<Fach> =
        this.filter { it.kuerzel !in fachData.conflictGroups.filter { g -> fach.kuerzel in g || fach2.kuerzel in g }.flatten() }
}