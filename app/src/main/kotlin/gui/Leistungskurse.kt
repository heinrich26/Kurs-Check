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

import com.kurswahlApp.data.*
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
    }

    private val lk1ComboBox: FachComboBox
    private val lk2ComboBox: FachComboBox
    private val helper: LeistungskurseLogik

    init {
        add(Box.createHorizontalStrut(50), column = 2)

        helper = LeistungskurseLogik(fachData, wahlData)

        lk1ComboBox = FachComboBox(FachComboBoxModel(helper.lk1Moeglichkeiten))

        val model2 = FachComboBoxModel(emptyList())
        lk2ComboBox = FachComboBox(model2)
        val listener: (Any) -> Unit = { notifier.invoke(isDataValid()) }
        lk1ComboBox.addActionListener(listener)
        lk2ComboBox.addActionListener(listener)

        lk1ComboBox.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) return@addItemListener

            lk2ComboBox.selectedItem = null

            model2.clear()
            model2.addAll(helper.getLk2Moeglichkeiten(it.item as Fach?))
        }
        // Sicherstellen, dass man LK2 auswählen kann
        if (wahlData.lk1 != null) {
            model2.addAll(helper.getLk2Moeglichkeiten(wahlData.lk1))
        }

        // Daten einsetzen
        lk1ComboBox.selectedItem = wahlData.lk1
        lk2ComboBox.selectedItem = wahlData.lk2
        listener.invoke(Any()) // auswahl überprüfen

        // Anzeigen
        // Margin hinzufügen

        val container = JPanel(GridBagLayout())
        container.border = TitledBorder(RoundedBorder(12), windowName.wrapTags("html", "b"))

        container.add(JLabel("1. LK "), row = 0, column = 0)
        container.add(JLabel("2. LK "), row = 1, column = 0)
        Insets(y = 1).let {
            container.add(lk1ComboBox, row = 0, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            container.add(lk2ComboBox, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

        container.add(Box.createHorizontalStrut(200), row = 0, column = 0, columnspan = 2)

        add(container)
    }


    override fun close(): KurswahlData = helper.save(lk1ComboBox.selectedItem!!, lk2ComboBox.selectedItem!!)

    override fun isDataValid(): Boolean = helper.validate(lk1ComboBox.selectedItem, lk2ComboBox.selectedItem)

    @Language("HTML")
    override fun showHelp(): String = "<h2>$windowName</h2><p>Die Leistungskurse ergeben sich wie folgt:<br><b>Ich bin leider nicht lyrisch begabt, deswegen beschwere dich bitte bei deinem/deiner PäKo, dass er/sie keine Hilfe verfasst hat!</b></p>"

    override val windowName: String
        get() = "Leistungskurse"
}
