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

import com.kurswahlApp.data.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
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
                        excludedItem = item to getIndexOf(item).also { i -> removeElementAt(i) }
                    }
                }
            }
        }
    }

    private val lk1: FachComboBox
    private val lk2: FachComboBox

    init {
        add(Box.createHorizontalStrut(50), column = 2)


        // Eine Fremdsprache, die erst in der Jahrgangsstufe 10 oder in der Einf??hrungsphase begonnen wurde,
        // darf nur als 3. oder 4. Pr??fungsfach oder als Referenzfach der 5. PK gew??hlt werden.
        val fs = wahlData.fremdsprachen.mapNotNull { (fach, jahr) -> if (jahr >= 10 ) null else fach }

        val model1 = FachComboBoxModel(fachData.lk1Moeglichkeiten.filter { !it.isFremdsprache || it in fs })
        lk1 = FachComboBox(model1)


        val wpfs = wahlData.wpfs
        val moeglichkeiten = fachData.lk2Moeglichkeiten.filter {
            /* Fach ist keine Fremdsprache bzw. Sch??ler hatte sie in Sek 1 */
            if (it.isFremdsprache) it in fs
            /* Hat keine WPF or Fach ist weder 1./2. WPF */
            else (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second)))
        }
        val model2 = LKComboBoxModel(moeglichkeiten, lk1)
        lk2 = FachComboBox(model2)
        val listener: (Any) -> Unit = { notifier.invoke(lk2.selectedItem != null) }
        lk1.addActionListener(listener)
        lk2.addActionListener(listener)
        listener.invoke(Any())


        lk1.renderer = FachRenderer
        lk2.renderer = FachRenderer


        // Daten einsetzen
        lk1.selectedItem = wahlData.lk1
        lk2.selectedItem = wahlData.lk2

        // Anzeigen
        // Margin hinzuf??gen

        val container = JPanel(GridBagLayout())
        container.border = TitledBorder(RoundedBorder(12), "Leistungskurse".wrapTags("html", "b"))

        container.add(JLabel("1. "), row = 0, column = 0)
        container.add(JLabel("2. "), row = 1, column = 0)
        Insets(1, 0, 1, 0).let {
            container.add(lk1, row = 0, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container.add(lk2, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

        add(container)
    }


    override fun close(): KurswahlData = wahlData.updateLKs(lk1 = lk1.selectedItem!!, lk2 = lk2.selectedItem!!)

    override fun isDataValid(): Boolean = (lk1.selectedItem != null && lk2.selectedItem != null)

    override val windowName: String
        get() = "Leistungskurse"
}