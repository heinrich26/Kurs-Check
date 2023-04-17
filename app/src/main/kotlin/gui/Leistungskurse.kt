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
import com.kurswahlApp.data.Wahlzeile.Companion.isWildcard
import org.intellij.lang.annotations.Language
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ItemEvent
import javax.swing.Box
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.TitledBorder


class Leistungskurse(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Leistungskurse(testKurswahl, testFachdata) }
        }

        class LKComboBoxModel(data: Collection<Fach>, other: FachComboBox) : FachComboBoxModel(data) {
            private var excludedItem: Pair<Fach, Int>? = null

            init {
                other.addItemListener {
                    if (it.stateChange == ItemEvent.SELECTED) {
                        if (excludedItem != null) {
                            insertElementAt(excludedItem!!.first, excludedItem!!.second)
                            excludedItem = null
                        }
                        val item = (it.item ?: return@addItemListener) as Fach

                        if (selectedItem == item)
                            selectedItem = null

                        excludedItem = with(getIndexOf(item)) {
                            if (this == -1) return@with null
                            removeElementAt(this)
                            item to this
                        }
                    }
                }
            }
        }
    }

    private val lk1: FachComboBox
    private val lk2: FachComboBox

    init {
        add(Box.createHorizontalStrut(50), column = 2)


        // Eine Fremdsprache, die erst in der Jahrgangsstufe 10 oder in der Einführungsphase begonnen wurde,
        // darf nur als 3. oder 4. Prüfungsfach oder als Referenzfach der 5. PK gewählt werden.
        val fs =
            wahlData.fremdsprachen.mapNotNull { (fach, jahr) -> if (jahr >= fachData.schultyp.ePhase) null else fach }

        val model1 = FachComboBoxModel(fachData.lk1Moeglichkeiten.filter {
            !it.isFremdsprache || it in fs && it.checkKlasse(wahlData.klasse)
        })
        lk1 = FachComboBox(model1)


        val wpfs = wahlData.wpfs
        val moeglichkeiten = fachData.lk2Moeglichkeiten.filter {
            it != lk1.selectedItem &&
                    /* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 */
                    (if (it.isFremdsprache) it in fs
                    /* Hat keine WPF or Fach ist weder 1./2. WPF */
                    else (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second))))
                    && it.checkKlasse(wahlData.klasse)
        }
        val model2 = LKComboBoxModel(moeglichkeiten, lk1)
        lk2 = FachComboBox(model2)
        val listener: (Any) -> Unit = { notifier.invoke(lk2.selectedItem != null) }
        lk1.addActionListener(listener)
        lk1.addActionListener {
            model2.clear()
            model2.addAll(LinkedHashSet<String>().apply {
                for ((lk11, lk21) in fachData.wahlzeilen.values) {
                    if (lk11.isWildcard && lk1.selectedItem != null && lk1.selectedItem!!.kuerzel in fachData.wzWildcards[lk11]!! || lk11 == lk1.selectedItem?.kuerzel)
                        if (lk21.isWildcard)
                            this.addAll(fachData.wzWildcards[lk21]!!)
                        else this.add(lk21)
                }
            }.map { fachData.faecherMap[it]!! }.filter {
                it != lk1.selectedItem &&
                        /* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 */
                        (if (it.isFremdsprache) it in fs
                        /* Hat keine WPF or Fach ist weder 1./2. WPF */
                        else (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second))))
                        && it.checkKlasse(wahlData.klasse)
            })
        }
        lk2.addActionListener(listener)
        listener.invoke(Any())


        lk1.renderer = FachRenderer
        lk2.renderer = FachRenderer


        // Daten einsetzen
        lk1.selectedItem = wahlData.lk1
        lk2.selectedItem = wahlData.lk2

        // Anzeigen
        // Margin hinzufügen

        val container = JPanel(GridBagLayout())
        container.border = TitledBorder(RoundedBorder(12), windowName.wrapTags("html", "b"))

        container.add(JLabel("1. LK "), row = 0, column = 0)
        container.add(JLabel("2. LK "), row = 1, column = 0)
        Insets(y = 1).let {
            container.add(lk1, row = 0, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container.add(lk2, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

        container.add(Box.createHorizontalStrut(200), row = 0, column = 0, columnspan = 2)

        add(container)
    }


    override fun close(): KurswahlData = wahlData.updateLKs(lk1 = lk1.selectedItem!!, lk2 = lk2.selectedItem!!)

    override fun isDataValid(): Boolean = (lk1.selectedItem != null && lk2.selectedItem != null)

    @Language("HTML")
    override fun showHelp(): String = "<h2>$windowName</h2><p>Die Leistungskurse ergeben sich wie folgt:<br><b>Ich bin leider nicht lyrisch begabt, deswegen beschwere dich bitte bei deinem PäKo, dass er/sie keine Hilfe verfasst hat!</b></p>"

    override val windowName: String
        get() = "Leistungskurse"
}
