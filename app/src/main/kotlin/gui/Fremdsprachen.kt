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
import java.awt.Insets
import java.awt.event.ActionEvent
import javax.swing.*


class Fremdsprachen(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Fremdsprachen(testKurswahl, testFachdata) }
        }
    }

    private val fsJahr1: SpinnerNumberModel
    private val fsJahr2: SpinnerNumberModel
    private val fsJahr3: SpinnerNumberModel
    private val fsJahr4: SpinnerNumberModel

    private val fs1: FachComboBox
    private val fs2: FachComboBox
    private val fs3: FachComboBox
    private val fs4: FachComboBox
    private val wpf1: FachComboBox
    private val wpf2: FachComboBox

    init {
        add(
            JLabel("Fremdsprachen:"),
            column = 0,
            columnspan = 2,
            margin = Insets(0, 0, 4, 0),
            anchor = GridBagConstraints.WEST
        )
        // Dummy damit das Fremdsprachen Label nicht alles verzerrt
        add(Box.createHorizontalStrut(50), column = 2)

        add(JLabel("ab Kl.:"), column = 3, anchor = GridBagConstraints.NORTHWEST)
        add(
            JLabel("Wahlpflicht:"),
            row = 5,
            column = 0,
            columnspan = 2,
            anchor = GridBagConstraints.WEST,
            margin = Insets(10, 0, 4, 0)
        )

        fsJahr4 = SpinnerNumberModel(1, 1, 10, 1)
        fsJahr3 = SpinnerNumberModel(1, 1, 10, 1)
        fsJahr2 = SpinnerNumberModel(1, 1, 10, 1)
        fsJahr1 = SpinnerNumberModel(1, 1, 10, 1)

        val spinner3 = JSpinner(fsJahr3)
        val spinner4 = JSpinner(fsJahr4)
        spinner3.isEnabled = false
        spinner4.isEnabled = false

        add(JSpinner(fsJahr1), row = 1, column = 3)
        add(JSpinner(fsJahr2), row = 2, column = 3)
        add(spinner3, row = 3, column = 3)
        add(spinner4, row = 4, column = 3)

        val model4 = ExclusiveComboBoxModel(fachData.fremdsprachen)
        fs4 = FachComboBox(model4)
        val model3 = ExclusiveComboBoxModel(fachData.fremdsprachen, fs4)
        fs3 = FachComboBox(model3)
        val model2 = ExclusiveComboBoxModel(fachData.fremdsprachen, fs3)
        fs2 = FachComboBox(model2)
        val model1 = ExclusiveComboBoxModel(fachData.fremdsprachen, fs2)
        fs1 = FachComboBox(model1)


        fs3.isEnabled = false
        fs4.isEnabled = false

        val listener3n4: (ActionEvent) -> Unit = { event ->
            if (event.actionCommand == "comboBoxChanged") {
                (model2.selectedItem != null).let {
                    fs3.isEnabled = it
                    spinner3.isEnabled = it
                }
                (model3.selectedItem != null).let {
                    fs4.isEnabled = it
                    spinner4.isEnabled = it
                }
            }
        }
        fs1.addActionListener(listener3n4)
        fs2.addActionListener(listener3n4)
        fs3.addActionListener { event ->
            if (event.actionCommand == "comboBoxChanged") {
                (model3.selectedItem != null).let {
                    fs4.isEnabled = it
                    spinner4.isEnabled = it
                }
            }
        }

        for (i in 1..4) {
            add(JLabel("$i."), row = i, column = 0)
        }

        fs1.renderer = FachRenderer
        fs2.renderer = FachRenderer
        fs3.renderer = FachRenderer
        fs4.renderer = FachRenderer

        // Daten einsetzen
        wahlData.fremdsprachen.let { fs ->
            val nFS = fs.size
            if (nFS != 0) { // Case: 1+ Items
                fs[0].let {
                    fs1.selectedItem = it.first
                    fsJahr1.value = it.second
                }
                if (nFS != 1) { // Case: 2+ Items
                    fs[1].let {
                        fs2.selectedItem = it.first
                        fsJahr2.value = it.second
                    }
                    if (nFS != 2) { // Case: 3+ Items
                        fs[2].let {
                            fs3.selectedItem = it.first
                            fsJahr3.value = it.second
                        }
                        if (nFS != 3) // Case: 4 Items
                            fs[3].let {
                                fs4.selectedItem = it.first
                                fsJahr4.value = it.second
                            }
                    }
                }
            }
        }


        val wpfModel2 = ExclusiveComboBoxModel(fachData.wpfs)
        wpf2 = FachComboBox(wpfModel2)

        val wpfModel1 = ExclusiveComboBoxModel(fachData.wpfs, wpf2)
        wpf1 = FachComboBox(wpfModel1)

        wpf1.renderer = FachRenderer
        wpf2.renderer = FachRenderer


        if (!fachData.zweiWPFs) {
            wpf2.isEnabled = false

            val checker = JCheckBox()
            wahlData.wpfs?.let {
                if (it.second != null) {
                    checker.isSelected = true
                    wpf2.isEnabled = true
                }
            }

            checker.addActionListener {
                if (!checker.isSelected) {
                    wpf2.isEnabled = false
                    wpf2.selectedIndex = 0
                } else wpf2.isEnabled = true
            }
            add(checker, row = 7, column = 0, anchor = GridBagConstraints.EAST)
        }

        // Daten einsetzen
        wahlData.wpfs?.let {
            wpf1.selectedItem = it.first
            wpf2.selectedItem = it.second
        }

        // Die hauptklasse informieren, ob die eingabe gültig ist
        wpf1.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                notifier.invoke(fs2.selectedItem != null && wpf1.selectedItem != null)
            }
        }


        fs2.addActionListener { notifier.invoke(fs2.selectedItem != null && wpf1.selectedItem != null) }

        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(fs1, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(fs2, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(fs3, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(fs4, row = 4, column = 1, fill = GridBagConstraints.BOTH, margin = it)

            add(wpf1, row = 6, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(wpf2, row = 7, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

    }


    override fun close(): KurswahlData {
        val sprachen: MutableList<Pair<Fach, Int>> =
            mutableListOf(fs1.selectedItem!! to fsJahr1.number as Int, fs2.selectedItem!! to fsJahr2.number as Int)
        fs3.selectedItem.let { sel ->
            if (sel != null) {
                sprachen.add(sel to fsJahr3.number as Int)
                fs4.selectedItem.let { if (it != null) sprachen.add(it to fsJahr4.number as Int) }
            }
        }

        return wahlData.updateWahlfaecher(
            fremdsprachenNew = sprachen,
            wpfsNew = wpf1.selectedItem!! to wpf2.selectedItem
        )
    }

    override fun isDataValid(): Boolean {
        return (fs1.selectedItem != null && fs2.selectedItem != null && wpf1.selectedItem != null && (!fachData.zweiWPFs || wpf2.selectedItem != null))
    }

    override val windowName: String
        get() = "Fremdsprachen & Wahlpflichtfächer"
}