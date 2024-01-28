/*
 * Copyright (c) 2022-2024  Hendrik Horstmann
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

    private val helper: PruefungsfaecherLogik

    init {
        helper = PruefungsfaecherLogik(fachData, wahlData).also { it._pf5Typ = pf5Typ::selectedItem }

        val pf3Model = FachComboBoxModel(helper.pf3Faecher())
        pf3 = FachComboBox(pf3Model).also { helper._pf3 = it::selected }

        val pf4Model = AwareFachComboBoxModel(pf3) { helper.pf4Faecher() }

        pf4 = FachComboBox(pf4Model).also { helper._pf4 = it::selected }

        val pf5Model = AwareFachComboBoxModel(pf3, pf4) { helper.pf5Faecher() }
        pf5 = FachComboBox(pf5Model).also { helper._pf5 = it::selected }

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
            pf5Model.addAll(helper.pf5Faecher())
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

    override fun close(): KurswahlData = helper.save()

    override fun isDataValid(): Boolean = helper.validate()

    @Language("HTML")
    override fun showHelp(): String = "<h2>$windowName</h2>\n<p>Die Prüfungsfächer ergeben sich wie folgt:<br><b>Ich bin leider nicht lyrisch begabt, deswegen beschwere dich bitte bei deinem PäKo, dass er/sie keine Hilfe verfasst hat!</b></p>\n"

    override val windowName: String
        get() = "Prüfungsfächer & 5. Prüfungskomponente"
}
