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
import com.kurswahlApp.data.*
import com.kurswahlApp.gui.AusgabeCheckBox.STYLE.*
import com.kurswahlApp.gui.UmfragePanel.Companion.toPanel
import gui.TitledPanel
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.HORIZONTAL
import java.awt.GridBagConstraints.WEST
import java.awt.GridBagLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.MatteBorder

class AusgabeLayout(fachData: FachData, wahlData: KurswahlData) : JPanel(GridBagLayout()) {
    private val checkboxMap = mutableMapOf<Fach, Array<AusgabeCheckBox>>()

    init {
        preferredSize = 614 by 942 // Din A4 + Page-Margin
        minimumSize = preferredSize
        add(JLabel(R.getString("overview")).apply { font = font.deriveFont(Font.BOLD, 18f) }, margin = Insets(left = 5, top = 5), anchor = WEST)


        val checkboxPanel = JPanel(VerticalLayout(alignment = VerticalLayout.EQUAL))

        // fremdsprachen & wpfs holen
        val fs = wahlData.fremdsprachen.map { it.first }
        val wpfs = wahlData.wpfs


        var feldPanel: JPanel? = null
        var feld = Int.MAX_VALUE
        for ((i, fach) in fachData.faecher.withIndex()) {
            if (!fach.isKurs) continue

            if (feld > 0 && feld != fach.aufgabenfeld) {
                if (feldPanel != null)
                    checkboxPanel.add(
                        feldPanel/*,
                        row = feld - 1,
                        column = 0,
                        fill = GridBagConstraints.BOTH,
                        weightx = 1.0*/
                    )
                feld = fach.aufgabenfeld
                feldPanel = TitledPanel(if (feld > 0) "${R.getString("aufgabenfeld")} $feld" else R.getString("additional_subjects"), layout = GridBagLayout())
            }

            // cond == true -> wählbar, sonst versteckt
            val cond: Boolean =
                fach in wahlData.kurse || // Nullpointer verhinden, falls nicht alle Fälle abgedeckt werden
                // if A: B else true == !A or B
                if (fach.isFremdsprache) fach in fs
                else (!fachData.strikteWPFs || !fach.brauchtWPF || (wpfs != null && fach in wpfs))


            if (cond) {
                val label = JLabel(fach.name)

                feldPanel!!.add(label, row = i, column = 0, fill = HORIZONTAL, weightx = 1.0)

                // Checkboxen bauen und hinzufügen
                checkboxMap[fach] =
                    Array(4) { AusgabeCheckBox() }.apply {
                        forEachIndexed { j, box -> feldPanel.add(box, row = i, column = j + 1, margin = Insets(4)) }
                    }
            }
        }
        checkboxPanel.add(feldPanel!!/*, row = 5, column = 0, fill = GridBagConstraints.BOTH, weightx = 1.0*/)

        for ((gk, choice) in wahlData.gks) { // auch für wahlData.pfs
            val row = checkboxMap[gk]!!
            for ((k, isActive) in choice.bools.withIndex()) {
                if (isActive) {
                    row[k].style = CHECKED
                }
            }
        }

        for ((fach, boxes) in checkboxMap.entries) {
            if (fach.nurIn != Wahlmoeglichkeit.DURCHGEHEND) {
                for ((box, b) in boxes.zip(fach.nurIn.bools))
                    if (!b) box.style = UNAVAILABLE
            }
        }

        for ((fach, style) in wahlData.pfs.filterNotNull().zip(listOf(LK, LK, PF3, PF4, PF5))) {
            for (box in checkboxMap[fach]!!) {
                // normale Fächer werden gesetzt und bereits angekreuzte zum entsprechenden Stil geändert
                if (fach.blockAsPf || box.style == CHECKED) box.style = style
            }
        }

        add(checkboxPanel, row = 1, column = 0, margin = Insets(4, 4, 4, 4), fill = GridBagConstraints.BOTH, weightx = 1.0, weighty = 1.0)


        // Extrainformationen
        // Panel mit allen anderen Infos
        feldPanel = TitledPanel(R.getString("your_infos")).apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = 0f
        }
        feldPanel.add(JLabel("${R.getString("type_of_pk5")}: <b>${wahlData.pf5Typ.repr}</b>".wrapHtml()))

        // Wahlzeile
        feldPanel.add(JLabel("${R.getString("wahlzeile")}: <b>${wahlData.wahlzeile}</b>".wrapHtml()))

        // Eintrittsjahr
        Calendar.getInstance().let {
            val year = it[Calendar.YEAR].let { year ->
                if (it.before(Calendar.Builder().setDate(year, Calendar.JULY, 1).build())) year else year + 1
            }
            feldPanel.add(JLabel("${R.getString("entry_year_sek2")}: ${year.toString().bold()}".wrapHtml()))
        }

        feldPanel.add(JLabel("${R.getString("first_name")}: <b>${wahlData.vorname}</b>".wrapHtml()))

        feldPanel.add(JLabel("${R.getString("last_name")}: <b>${wahlData.nachname}</b>".wrapHtml()))

        feldPanel.add(
            JLabel("${R.getString("born")}: <b>${wahlData.geburtsdatum!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}</b> in <b>${wahlData.geburtsort}</b>".wrapHtml())
        )

        feldPanel.add(
            JLabel("${R.getString("nationality")}: <b>${Locale(Locale.getDefault().language, wahlData.staatsangehoerigkeit).displayCountry}</b>".wrapHtml())
        )

        checkboxPanel.add(feldPanel)

        feldPanel = TitledPanel(R.getString("course_count"), layout = GridBagLayout())


        // Kursanzahlen
        val anzahlen = wahlData.countCourses()
        val font = JLabel().font.deriveFont(Font.BOLD)
        for ((i, n) in anzahlen.withIndex()) {
            feldPanel.add(JLabel("Q${i + 1}"), column = 2*i, anchor = WEST, weightx = 0.266)
            feldPanel.add(JLabel("$n").also { it.font = font }, column = 2 * i + 1, anchor = WEST, weightx = 1.0)
        }

        feldPanel.add(JLabel("="), column = 8, anchor = WEST, weightx = 0.3)
        feldPanel.add(JLabel("${anzahlen.sum()}").also { it.font = font }, column = 9)
        checkboxPanel.add(feldPanel)

        // Umfragen
        if (fachData.umfragen.isNotEmpty()) {
            feldPanel = TitledPanel(R.getString("survey"), layout = GridBagLayout())

            fachData.umfragen.mapIndexed { i, umfrage ->
                umfrage
                    .toPanel()
                    .constructors.first()
                    .call(umfrage, wahlData.umfrageData.getOrNull(i), { _: Boolean -> }, true)
                    .also { feldPanel.add(it, fill = HORIZONTAL, row = i) }
            }
            checkboxPanel.add(feldPanel)
        }


        // Unterschriftfeld
        feldPanel = TitledPanel(R.getString("signature"), layout = GridBagLayout())
        for (i in 0..1) {
            feldPanel.add(JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("d.M.yyyy"))).also {
                Color(120, 120, 120).let { gray ->
                    it.border = MatteBorder(0, 0, 2, 0, gray)
                    it.foreground = gray
                }
                it.font = it.font.deriveFont(12f)
            },
                fill = HORIZONTAL,
                margin = Insets(top = if (i==1) 8 else 0),
                weightx = 1.0,
                row = i
            )
        }

        checkboxPanel.add(Box.createVerticalStrut(4))
        checkboxPanel.add(feldPanel)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            KurswahlPanel.runTest(614, 874) { AusgabeLayout(testFachdata, testKurswahl) }
        }
    }


}