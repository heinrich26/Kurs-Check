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

import com.github.lgooddatepicker.components.DatePicker
import com.github.lgooddatepicker.components.DatePickerSettings
import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import org.intellij.lang.annotations.Language
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.geom.GeneralPath
import java.time.LocalDate
import java.util.*
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class Nutzerdaten(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit) :
    KurswahlPanel(wahlData, fachData, notifier) {

    private val staatsangehoerigkeitPicker: JComboBox<String> = JComboBox(COUNTRY_CODES)
    private val geburtsdatumPicker = DatePicker()

    private val vornameEntry = HintTextField("Vornamen", wahlData.vorname)
    private val nachnameEntry = HintTextField("Nachname", wahlData.nachname)
    private val geburtsortEntry = HintTextField("Geburtsort", wahlData.geburtsort)

    init {
        with(geburtsdatumPicker.componentToggleCalendarButton) {
            icon = CalendarIcon()
            text = null
            border = null
        }

        with(geburtsdatumPicker.componentDateTextField) {
            JTextField().let {
                border = it.border
                margin = it.margin
            }
        }

        with(geburtsdatumPicker.settings) {
            allowEmptyDates = false
            setColorBackgroundWeekNumberLabels(Consts.COLOR_PRIMARY, true)
            setColorBackgroundWeekdayLabels(Consts.COLOR_PRIMARY, true)
            setColor(DatePickerSettings.DateArea.CalendarTextWeekdays, Color.WHITE)
            setColor(DatePickerSettings.DateArea.CalendarBackgroundSelectedDate, Consts.COLOR_CONTROL)
            setColor(DatePickerSettings.DateArea.CalendarBorderSelectedDate, Consts.COLOR_CONTROL)
            setColor(DatePickerSettings.DateArea.BackgroundCalendarPanelLabelsOnHover, Color(223, 205, 251))
            this.visibleTodayButton = false

            setFormatForDatesCommonEra("dd.MM.yyyy")
            setFormatForDatesBeforeCommonEra("dd.MM.uuuu")
            LocalDate.now().let {
                geburtsdatumPicker.date = LocalDate.of(it.year - 15, 1, 1)
                if (it.isBefore(LocalDate.of(it.year, 7, 1))) it.year else it.year + 1
            }.let {
                setDateRangeLimits(
                    LocalDate.of(it - Consts.MAX_ALTER, 7, 1),
                    LocalDate.of(it - Consts.MIN_ALTER, 6, 30)
                )
            }
        }

        staatsangehoerigkeitPicker.selectedItem = wahlData.staatsangehoerigkeit
        staatsangehoerigkeitPicker.selectedIndex = COUNTRY_CODES.indexOf(wahlData.staatsangehoerigkeit)
        staatsangehoerigkeitPicker.renderer = CountryRenderer
        geburtsdatumPicker.date = wahlData.geburtsdatum


        val container = JPanel(GridBagLayout())
        container.border = TitledBorder(RoundedBorder(12), windowName.wrapTags("html", "b"))
        // Beschreibungen hinzufügen
        container.add(JLabel("Vornamen (alle)"), row = 0, anchor = GridBagConstraints.WEST)
        container.add(JLabel("Nachname"), row = 1, anchor = GridBagConstraints.WEST)
        container.add(JLabel("Geburtsort"), row = 2, anchor = GridBagConstraints.WEST)
        container.add(JLabel("Geburtsdatum"), row = 3, anchor = GridBagConstraints.WEST)
        container.add(JLabel("Staatsangehörigkeit"), row = 4, anchor = GridBagConstraints.WEST)

        // Felder mit genügen Abstand anzeigen
        Insets(2).let {
            container.add(vornameEntry, row = 0, column = 1, margin = it, fill = GridBagConstraints.BOTH)
            container.add(nachnameEntry, row = 1, column = 1, margin = it, fill = GridBagConstraints.BOTH)
            container.add(geburtsortEntry, row = 2, column = 1, margin = it, fill = GridBagConstraints.BOTH)
            container.add(geburtsdatumPicker, row = 3, column = 1, margin = it, fill = GridBagConstraints.BOTH)
            container.add(staatsangehoerigkeitPicker, row = 4, column = 1, margin = it, fill = GridBagConstraints.BOTH)
        }


        val docListener = object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent) = Unit
            override fun insertUpdate(e: DocumentEvent) = notifier.invoke(isDataValid())
            override fun removeUpdate(e: DocumentEvent) = notifier.invoke(isDataValid())
        }

        vornameEntry.document.addDocumentListener(docListener)
        nachnameEntry.document.addDocumentListener(docListener)
        geburtsortEntry.document.addDocumentListener(docListener)
        geburtsdatumPicker.componentDateTextField.document.addDocumentListener(docListener)
        staatsangehoerigkeitPicker.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) notifier.invoke(
                isDataValid()
            )
        }

        add(container)

        notifier.invoke(isDataValid())
    }


    override fun close(): KurswahlData = wahlData.copy(
        vorname = vornameEntry.text.trim(),
        nachname = nachnameEntry.text.trim(),
        geburtsort = geburtsortEntry.text.trim(),
        geburtsdatum = geburtsdatumPicker.date,
        staatsangehoerigkeit = staatsangehoerigkeitPicker.selectedItem as String
    )

    override fun isDataValid(): Boolean {
        return vornameEntry.text.isNotBlank() && nachnameEntry.text.isNotBlank() && geburtsortEntry.text.isNotBlank() && (geburtsdatumPicker.isTextFieldValid && geburtsdatumPicker.date != null)
    }

    @Language("HTML")
    override fun showHelp(): String =
        "<h2>$windowName</h2><p>Hier musst du deine eigenen Daten eingeben! Das kriegst du doch hin, oder?</p>"

    override val windowName: String
        get() = "Persönliche Daten"

    companion object {
        private val COUNTRY_CODES = Locale.getISOCountries()

        private val CALENDAR_SHAPE = GeneralPath().apply {
            moveTo(17.0, 2.0)
            curveTo(16.4, 2.0, 16.0, 2.4, 16.0, 3.0)
            lineTo(16.0, 4.0)
            lineTo(8.0, 4.0)
            lineTo(8.0, 3.0)
            curveTo(8.0, 2.4, 7.6, 2.0, 7.0, 2.0)
            curveTo(6.4, 2.0, 6.0, 2.4, 6.0, 3.0)
            lineTo(6.0, 4.0)
            lineTo(5.0, 4.0)
            curveTo(3.9, 4.0, 3.0, 4.9, 3.0, 6.0)
            lineTo(3.0, 20.0)
            curveTo(3.0, 21.1, 3.9, 22.0, 5.0, 22.0)
            lineTo(19.0, 22.0)
            curveTo(20.1, 22.0, 21.0, 21.1, 21.0, 20.0)
            lineTo(21.0, 6.0)
            curveTo(21.0, 4.9, 20.1, 4.0, 19.0, 4.0)
            lineTo(18.0, 4.0)
            lineTo(18.0, 3.0)
            curveTo(18.0, 2.4, 17.6, 2.0, 17.0, 2.0)
            closePath()
            moveTo(19.0, 20.0)
            lineTo(5.0, 20.0)
            lineTo(5.0, 10.0)
            lineTo(19.0, 10.0)
            lineTo(19.0, 20.0)
            closePath()
            moveTo(11.0, 13.0)
            curveTo(11.0, 12.4, 11.4, 12.0, 12.0, 12.0)
            curveTo(12.6, 12.0, 13.0, 12.4, 13.0, 13.0)
            curveTo(13.0, 13.0, 12.6, 14.0, 12.0, 14.0)
            curveTo(12.0, 14.0, 11.0, 13.6, 11.0, 13.0)
            closePath()
            moveTo(7.0, 13.0)
            curveTo(7.0, 12.4, 7.4, 12.0, 8.0, 12.0)
            curveTo(8.6, 12.0, 9.0, 12.4, 9.0, 13.0)
            curveTo(9.0, 13.0, 8.6, 14.0, 8.0, 14.0)
            curveTo(8.0, 14.0, 7.0, 13.6, 7.0, 13.0)
            closePath()
            moveTo(15.0, 13.0)
            curveTo(15.0, 12.4, 15.4, 12.0, 16.0, 12.0)
            curveTo(16.6, 12.0, 17.0, 12.4, 17.0, 13.0)
            curveTo(17.0, 13.0, 16.6, 14.0, 16.0, 14.0)
            curveTo(16.0, 14.0, 15.0, 13.6, 15.0, 13.0)
            closePath()
            moveTo(11.0, 17.0)
            curveTo(11.0, 16.4, 11.4, 16.0, 12.0, 16.0)
            curveTo(12.6, 16.0, 13.0, 16.4, 13.0, 17.0)
            curveTo(13.0, 17.0, 12.6, 18.0, 12.0, 18.0)
            curveTo(12.0, 18.0, 11.0, 17.6, 11.0, 17.0)
            closePath()
            moveTo(7.0, 17.0)
            curveTo(7.0, 16.4, 7.4, 16.0, 8.0, 16.0)
            curveTo(8.6, 16.0, 9.0, 16.4, 9.0, 17.0)
            curveTo(9.0, 17.0, 8.6, 18.0, 8.0, 18.0)
            curveTo(8.0, 18.0, 7.0, 17.6, 7.0, 17.0)
            closePath()
            moveTo(15.0, 17.0)
            curveTo(15.0, 16.4, 15.4, 16.0, 16.0, 16.0)
            curveTo(16.6, 16.0, 17.0, 16.4, 17.0, 17.0)
            curveTo(17.0, 17.0, 16.6, 18.0, 16.0, 18.0)
            curveTo(16.0, 18.0, 15.0, 17.6, 15.0, 17.0)
            closePath()
        }


        object CountryRenderer : DefaultListCellRenderer() {
            private fun readResolve(): Any = CountryRenderer

            private val names = Locale.getDefault().language.let { lang ->
                COUNTRY_CODES.associateWith { Locale(lang, it).displayCountry }
            }

            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ): Component = super.getListCellRendererComponent(
                list, names.getOrDefault(value, "Auswählen"), index, isSelected, cellHasFocus
            )
        }
    }

    private inner class CalendarIcon : Icon {
        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            val g2D = g as Graphics2D

            g2D.setRenderingHints(Consts.RENDERING_HINTS)


            g2D.color = Consts.COLOR_PRIMARY
            g2D.fill(CALENDAR_SHAPE)

            g2D.dispose()
        }

        override fun getIconWidth() = 24

        override fun getIconHeight() = 24
    }

}