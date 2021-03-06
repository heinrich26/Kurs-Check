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

import com.github.lgooddatepicker.components.DatePicker
import com.kurswahlApp.data.Consts.MAX_ALTER
import com.kurswahlApp.data.Consts.MIN_ALTER
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import java.awt.GridBagConstraints
import java.awt.Insets
import java.time.LocalDate
import javax.swing.JComboBox
import javax.swing.JLabel

class Nutzerdaten(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit) :
    KurswahlPanel(wahlData, fachData, notifier) {

    private val staatsangehoerigkeitPicker: JComboBox<String> = JComboBox(COUNTRY_CODES)
    private val geburtsdatumPicker = DatePicker()

    private val vornameEntry = HintTextField("Vornamen", wahlData.vorname, columns = 12)
    private val nachnameEntry = HintTextField("Nachname", wahlData.nachname, columns = 12)
    private val geburtsortEntry = HintTextField("Geburtsort", wahlData.geburtsort, columns = 12)

    init {
        with(geburtsdatumPicker.settings) {
            allowEmptyDates = false
            setFormatForDatesCommonEra("dd.MM.yy")
            setFormatForDatesBeforeCommonEra("dd.MM.uu")
            LocalDate.now().let {
                geburtsdatumPicker.date = LocalDate.of(it.year - 15, 1, 1)
                it.year.let { year -> if (it.isBefore(LocalDate.of(year, 7, 1))) year else year + 1 }
            }.let {
                setDateRangeLimits(LocalDate.of(it - MAX_ALTER, 7, 1), LocalDate.of(it - MIN_ALTER, 6, 30))
            }
        }

        staatsangehoerigkeitPicker.selectedItem = wahlData.staatsangehoerigkeit
        geburtsdatumPicker.date = wahlData.geburtsdatum

        // Beschreibungen hinzuf??gen
        add(JLabel("Vornamen (alle)"), row = 0, anchor = GridBagConstraints.WEST)
        add(JLabel("Nachname"), row = 1, anchor = GridBagConstraints.WEST)
        add(JLabel("Geburtsort"), row = 2, anchor = GridBagConstraints.WEST)
        add(JLabel("Geburtsdatum"), row = 3, anchor = GridBagConstraints.WEST)
        add(JLabel("Staatsangeh??rigkeit"), row = 4, anchor = GridBagConstraints.WEST)

        // Felder mit gen??gen Abstand anzeigen
        Insets(2, 0, 2, 0).let {
            add(vornameEntry, row = 0, column = 1, margin = it)
            add(nachnameEntry, row = 1, column = 1, margin = it)
            add(geburtsortEntry, row = 2, column = 1, margin = it)
            add(geburtsdatumPicker, row = 3, column = 1, margin = it, fill = GridBagConstraints.BOTH)
            add(staatsangehoerigkeitPicker, row = 4, column = 1, margin = it)
        }

        notifier.invoke(true)
    }


    override fun close(): KurswahlData = wahlData.copy(
        vorname = vornameEntry.text.trim(),
        nachname = nachnameEntry.text.trim(),
        geburtsort = geburtsortEntry.text.trim(),
        geburtsdatum = geburtsdatumPicker.date,
        staatsangehoerigkeit = staatsangehoerigkeitPicker.selectedItem as String
    )

    override fun isDataValid(): Boolean =
        vornameEntry.text.isNotBlank() && nachnameEntry.text.isNotBlank() && geburtsortEntry.text.isNotBlank()
                && (geburtsdatumPicker.isTextFieldValid && geburtsdatumPicker.date != null)

    override val windowName: String
        get() = "Pers??nliche Daten"

    companion object {
        // TODO evtl. L??ndernamen anzeigen (https://www.codejava.net/java-se/swing/java-swing-country-list-combobox-example)
        private val COUNTRY_CODES = java.util.Locale.getISOCountries()
    }
}