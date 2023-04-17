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

package com.kurswahlApp.gui

import com.kurswahlApp.data.*
import com.kurswahlApp.data.Wahlzeile.Companion.isAny
import com.kurswahlApp.data.Wahlzeile.Companion.isWildcard
import com.kurswahlApp.data.WahlzeileLinientyp.*
import org.intellij.lang.annotations.Language
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel


class Pruefungsfaecher(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Pruefungsfaecher(testKurswahl, testFachdata) }
        }

        class AwareFachComboBoxModel(vararg vorgaenger: FachComboBox, generator: () -> Collection<Fach>) :
            FachComboBoxModel(generator()) {
            init {
                for (comboBox in vorgaenger) {
                    comboBox.addActionListener {
                        this.removeAllElements()
                        this.addAll(generator())
                    }
                }
            }
        }
    }

    private val pf3: FachComboBox
    private val pf4: FachComboBox
    private val pf5: FachComboBox


    private val pf5Typ = JComboBox(Pf5Typ.values())

    private val userFs = wahlData.fremdsprachen.map { it.first }
    private val userWpfs = wahlData.wpfs
    private val filteredZeilen = fachData.filterWahlzeilen(wahlData.lk1, wahlData.lk2)
    private val filteredFaecherPf5 = fachData.faecherMap.filterValues {
        /* Nach VO-GO Berlin - § 23 Wahl der Prüfungsfächer Nr.8 muss das Fach für die 5. PK lediglich 4
        Semester belegt werden und bei einer Fremdsprache muss diese in Klasse 10/der E-Phase erlernt
        worden sein. */
        // ist als Kurs wählbar
        it.isKurs &&
                // Darf mit der besuchten Klasse gewählt werden
                it.checkKlasse(wahlData.klasse) &&
                // ist kein Zusatzkurs & ...
                it.aufgabenfeld != -1 &&
                // Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1
                (!it.isFremdsprache || it in userFs)

    }
    private val filteredFaecher = filteredFaecherPf5.filterValues {
        // Fach ist kein LK
        it != wahlData.lk1 && it != wahlData.lk2 &&
                // Braucht kein WPF oder Fach ist eins von 1./2. WPF
                (!it.brauchtWPF || (userWpfs != null && (it == userWpfs.first || it == userWpfs.second)))
    }

    private val zeilenFuerFuenfte: MutableSet<Pair<Int, Wahlzeile>> = mutableSetOf()

    init {
        val pf3faecher = pf3Faecher()


        val pf3Model = FachComboBoxModel(pf3faecher)
        pf3 = FachComboBox(pf3Model)

        val pf4Model = AwareFachComboBoxModel(pf3) { pf4Faecher() }
        pf4 = FachComboBox(pf4Model)

        val pf5Model = AwareFachComboBoxModel(pf3, pf4) { pf5Faecher() }
        pf5 = FachComboBox(pf5Model)


        pf3.renderer = FachRenderer
        pf4.renderer = FachRenderer
        pf5.renderer = FachRenderer
        pf5Typ.renderer = Pf5Typ.Renderer


        // Daten einsetzen
        pf3.selectedItem = wahlData.pf3
        pf4.selectedItem = wahlData.pf4
        pf5.selectedItem = wahlData.pf5
        pf5Typ.selectedItem = wahlData.pf5_typ

        pf3.addActionListener { notifier.invoke(pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null) }
        pf4.addActionListener { notifier.invoke(pf4.selectedItem != null && pf5.selectedItem != null) }
        pf5.addActionListener { notifier.invoke(pf5.selectedItem != null) }
        notifier.invoke(pf5.selectedItem != null)

        pf5Typ.addActionListener {
            notifier.invoke(false)
            pf5Model.removeAllElements()
            pf5Model.addAll(pf5Faecher())
            pf5.selectedItem = null
        }


        val container = JPanel(GridBagLayout())
        container.border = RoundedBorder(12)
        // Anzeigen
        // Margin hinzufügen
        Insets(y = 1).let {
            container.add(pf3, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container.add(pf4, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container.add(pf5, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container.add(pf5Typ, row = 3, column = 3, margin = it)
        }

        // Beschriftungen hinzufügen
        container.add(JLabel("3. PF "), row = 1, column = 0)
        container.add(JLabel("4. PF "), row = 2, column = 0)
        container.add(JLabel("5. PK "), row = 3, column = 0)
        container.add(JLabel(" als "), row = 3, column = 2)

        add(container)
    }

    private fun pf3Faecher(): Collection<Fach> {
        val kuerzel = mutableSetOf<String>()

        val beliebig: () -> List<Fach> = { filteredFaecher.mapNotNull { if (it.value.nurPf4_5) null else it.value } }

        for ((_, _, pf3, pf4, pf5, linien) in filteredZeilen.values) {
            if (pf3.isAny) return beliebig()

            if (pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf3]!!)
            else kuerzel.add(pf3)

            if (linien != DURCHGEZOGEN) {
                if (pf4.isAny) return beliebig()

                if (pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf4]!!)
                else kuerzel.add(pf4)

                if (linien != KEINE_DURCHGEZOGEN) {
                    if (pf5.isAny) return beliebig()

                    if (pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf5]!!)
                    else kuerzel.add(pf5)
                }
            }
        }

        return kuerzel.mapNotNull {
            val fach = filteredFaecher[it] ?: return@mapNotNull null
            if (fach.nurPf4_5) null else fach
        }
    }

    private fun pf4Faecher(): List<Fach> {
        val selectedPf3 = pf3.selectedItem ?: return emptyList()
        val pf3Groups = fachData.wzWildcardMapping[selectedPf3]!!

        val beliebig: () -> List<Fach> = {
            if (selectedPf3.kuerzel in fachData.pf3_4AusschlussFaecher)
                filteredFaecher.values.filter { it.kuerzel !in fachData.pf3_4AusschlussFaecher }
            else filteredFaecher.values.filter { it != selectedPf3 }
        }

        val kuerzel = mutableSetOf<String>()
        for ((_, _, pf3, pf4, pf5, linien) in filteredZeilen.values) {
            if (pf3 in pf3Groups) {
                if (pf4.isAny) return beliebig()

                if (pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf4]!!)
                else kuerzel.add(pf4)

                if (linien == GESTRICHELT || linien == KEINE) {
                    if (pf5.isAny) return beliebig()

                    if (pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf5]!!)
                    else kuerzel.add(pf5)
                }
            }

            if (linien != DURCHGEZOGEN) {
                if (pf4 in pf3Groups) {
                    if (pf3.isAny) return beliebig()

                    if (pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf3]!!)
                    else kuerzel.add(pf3)

                    if (linien == KEINE) {
                        if (pf5.isWildcard) return beliebig()

                        if (pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf5]!!)
                        else kuerzel.add(pf5)
                    }
                }

                if (pf5 in pf3Groups) {
                    if (pf3.isAny) return beliebig()

                    if (linien == KEINE) {
                        if (pf4.isAny) return beliebig()

                        if (pf4.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf4]!!)
                        else kuerzel.add(pf4)
                    }

                    if (pf3.isWildcard) kuerzel.addAll(fachData.wzWildcards[pf3]!!)
                    else kuerzel.add(pf3)
                }
            }
        }

        if (selectedPf3.kuerzel in fachData.pf3_4AusschlussFaecher) kuerzel.removeAll(fachData.pf3_4AusschlussFaecher)
        kuerzel.remove(selectedPf3.kuerzel) // Prüfungsfach 3 nicht 2x wählen
        return kuerzel.mapNotNull { filteredFaecher[it] }
    }

    @Suppress("ConvertArgumentToSet")
    private fun pf5Faecher(): List<Fach> {
        val selectedPf3 = pf3.selectedItem ?: return emptyList()
        val pf3Groups = fachData.wzWildcardMapping[selectedPf3]!!

        val selectedPf4 = pf4.selectedItem ?: return emptyList()
        val pf4Groups = fachData.wzWildcardMapping[selectedPf4]!!


        // bei Präsentation bereits gewählte entfernen!
        val beliebig: () -> List<Fach> = {
            if (pf5Typ.selectedItem as Pf5Typ == Pf5Typ.PRAESENTATION)
                filteredFaecherPf5.values.toMutableList().apply {
                    removeAll(arrayOf(wahlData.lk1!!, wahlData.lk2!!, selectedPf3, selectedPf4))
                }.toList()
            else filteredFaecherPf5.values.toList()
        }

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

            if (wz.linien != DURCHGEZOGEN) {
                // Fall: keine|durchgezogen; 3./4. getauscht
                if (wz.pf4 in pf3Groups && wz.pf3 in pf4Groups) {
                    zeilenFuerFuenfte.add(5 to wz)
                    if (wz.pf5.isAny) return beliebig()

                    if (wz.pf5.isWildcard) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                    else kuerzel.add(wz.pf5)
                }
                // Fall: gestrichelte Linien oder keine Linien
                //       3./5. oder 4./5. getauscht
                if (wz.linien != KEINE_DURCHGEZOGEN) {
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
        if (pf5Typ.selectedItem as Pf5Typ == Pf5Typ.PRAESENTATION) {
            kuerzel.removeAll(
                arrayOf(wahlData.lk1!!.kuerzel, wahlData.lk2!!.kuerzel, selectedPf3.kuerzel, selectedPf4.kuerzel)
            )
        }

        return kuerzel.mapNotNull { filteredFaecher[it] }
    }

    override fun close(): KurswahlData {
        val selectedPf5 = pf5.selectedItem!!
        val pf5Groups = fachData.wzWildcardMapping[selectedPf5]!!
        var zeile: Int = -1
        for ((field, wz) in zeilenFuerFuenfte) {
            val fieldVal = when (field) {
                3 -> wz.pf3
                4 -> wz.pf4
                else /* 5 */ -> wz.pf5
            }
            if (fieldVal in pf5Groups) {
                zeile = filteredZeilen.firstNotNullOf { if (it.value == wz) it.key else null }
                break
            }
        }

        return wahlData.updatePFs(
            pf3 = pf3.selectedItem!!,
            pf4 = pf4.selectedItem!!,
            pf5 = selectedPf5,
            pf5_typ = pf5Typ.selectedItem as Pf5Typ,
            wahlzeile = zeile
        )
    }

    override fun isDataValid(): Boolean =
        (pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null)

    @Language("HTML")
    override fun showHelp(): String = "<h2>$windowName</h2>\n<p>Die Prüfungsfächer ergeben sich wie folgt:<br><b>Ich bin leider nicht lyrisch begabt, deswegen beschwere dich bitte bei deinem PäKo, dass er/sie keine Hilfe verfasst hat!</b></p>\n"

    override val windowName: String
        get() = "Prüfungsfächer & 5. Prüfungskomponente"
}
