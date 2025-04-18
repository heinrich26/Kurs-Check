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

package com.kurswahlApp.gui

import com.kurswahlApp.R
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import com.kurswahlApp.data.testFachdata
import com.kurswahlApp.data.testKurswahl
import org.intellij.lang.annotations.Language
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.TitledBorder


class Fremdsprachen(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Fremdsprachen(testKurswahl, testFachdata) }
        }
    }

    //#region Selection Components
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

    private val klasse: JComboBox<String?>
    //#endregion

    init {
        val ePhase = fachData.schultyp.ePhase

        val container1 = JPanel(GridBagLayout())
        val container2 = JPanel(GridBagLayout())
        val container3 = JPanel(GridBagLayout())
        RoundedBorder(12).let {
            container1.border = TitledBorder(it, "Fremdsprachen".wrapTags("html", "b"))
            container2.border = TitledBorder(it, "Wahlpflichtfächer (Kl. $ePhase)".wrapTags("html", "b"))
            container3.border = TitledBorder(it, "Klasse".wrapTags("html", "b"))
        }


        container1.add(JLabel("ab Kl.:"), column = 3, anchor = GridBagConstraints.NORTHWEST)

        fsJahr4 = SpinnerNumberModel(1, 1, ePhase, 1)
        fsJahr3 = SpinnerNumberModel(1, 1, ePhase, 1)
        fsJahr2 = SpinnerNumberModel(1, 1, ePhase, 1)
        fsJahr1 = SpinnerNumberModel(1, 1, ePhase, 1)

        val spinner3 = JSpinner(fsJahr3)
        val spinner4 = JSpinner(fsJahr4)
        spinner3.isEnabled = false
        spinner4.isEnabled = false

        container1.add(JSpinner(fsJahr1), row = 1, column = 3)
        container1.add(JSpinner(fsJahr2), row = 2, column = 3)
        container1.add(spinner3, row = 3, column = 3)
        container1.add(spinner4, row = 4, column = 3)

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
            container1.add(JLabel("$i."), row = i, column = 0)
        }

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


        if (!fachData.zweiWPFs) {
            wpf2.isEnabled = false

            val checker = JCheckBox()
            wahlData.wpfs?.let {
                if (it.second != null) {
                    checker.isSelected = true
                    wpf2.isEnabled = true
                }
            }

            checker.toolTipText = R.getString("zweiWPFsTooltip")

            checker.addActionListener {
                if (!checker.isSelected) {
                    wpf2.isEnabled = false
                    wpf2.selectedIndex = 0
                } else wpf2.isEnabled = true
            }
            container2.add(checker, row = 1, column = 0, anchor = GridBagConstraints.EAST)
        }


        // Wahl der Klasse
        klasse = JComboBox(DefaultComboBoxModel(arrayOf(null, *fachData.klassen.toTypedArray())))
        klasse.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ): Component? = super.getListCellRendererComponent(
                list, value ?: "Standard", index, isSelected, cellHasFocus
            )
        }
        klasse.selectedItem = wahlData.klasse


        // Daten einsetzen
        wahlData.wpfs?.let {
            wpf1.selectedItem = it.first
            wpf2.selectedItem = it.second
        }


        // Die Hauptklasse informieren, wenn sich die Eingabe ändert/ob die Eingabe gültig ist.
        val callback: (it: ActionEvent) -> Unit =
            if (fachData.zweiWPFs) { it ->
                if (it.actionCommand == "comboBoxChanged") notifier.invoke(fs2.selectedItem != null && wpf1.selectedItem != null && wpf2.selectedItem != null)
            } else { it ->
                if (it.actionCommand == "comboBoxChanged") notifier.invoke(fs2.selectedItem != null && wpf1.selectedItem != null)
            }
        wpf1.addActionListener(callback)
        wpf2.addActionListener(callback)
        fs2.addActionListener(callback)

        // Anzeigen
        // Margin hinzufügen
        Insets(x = 2, y = 1).let {
            container1.add(fs1, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container1.add(fs2, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container1.add(fs3, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container1.add(fs4, row = 4, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

        container2.add(wpf1, row = 0, column = 1, fill = GridBagConstraints.BOTH, margin = Insets(bottom = 2))
        container2.add(wpf2, row = 1, column = 1, fill = GridBagConstraints.BOTH)

        container3.add(klasse, row = 9, column = 1, fill = GridBagConstraints.BOTH)

        add(container1, row = 0, fill = GridBagConstraints.BOTH)

        // Wahlpflichtfach automatisch auswählen und ausblenden, wenn es nur eine Auswahlmöglichkeit gibt
        if (fachData.wpfs.size == 1) {
            wpf1.selectedItem = fachData.wpfs.first()
        } else {
            add(container2, row = 1, fill = GridBagConstraints.BOTH)
        }

        if (fachData.klassen.isNotEmpty()) add(container3, row = 2, fill = GridBagConstraints.BOTH)
        add(Box.createHorizontalStrut(200), row = 0, column = 0)
    }


    override fun close(): KurswahlData = wahlData.updateWahlfaecher(
        fremdsprachenNew = buildList(4) {
            add(fs1.selectedItem!! to fsJahr1.number as Int)
            add(fs2.selectedItem!! to fsJahr2.number as Int)
            fs3.selectedItem?.let { fach ->
                add(fach to fsJahr3.number as Int)
                fs4.selectedItem?.let { add(it to fsJahr4.number as Int) }
            }
        },
        wpfsNew = wpf1.selectedItem!! to wpf2.selectedItem,
        klasse = klasse.selectedItem as String?
    )

    override fun isDataValid(): Boolean =
        fs1.selectedItem != null &&
                fs2.selectedItem != null &&
                wpf1.selectedItem != null &&
                (!fachData.zweiWPFs || wpf2.selectedItem != null)

    @Language("HTML")
    override fun showHelp(): String =
        "<h2>$windowName</h2><p>Hier musst du deine Fremdsprachen, deine Wahlpflichtfächer der 10. Klasse und eventuell deine Klasse auswählen.<br><b>Ich bin leider nicht lyrisch begabt, deswegen beschwere dich bitte bei deinem PäKo, dass er/sie keine hilfreichere Hilfe verfasst hat!</b></p>"

    override val windowName: String = "Fremdsprachen & Wahlpflichtfächer"
}
