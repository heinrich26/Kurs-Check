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
import org.intellij.lang.annotations.Language
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.*
import javax.swing.JScrollPane


class Overview(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData = wahlData

    override fun isDataValid(): Boolean = true // TODO Finales Checking?

    @Language("HTML")
    override fun showHelp(): String = "<ol>\n    <li><a href='#aufbau'>Aufbau</a></li>\n    <li><a href='#ablauf'>Ablauf</a></li>\n    <li><a href='#speichern'>Speichern</a></li>\n    <li><a href='#copyright'>Copyright</a></li>\n</ol>\n<h2><a name='aufbau'>Aufbau</a></h2>\n${img("docs/start_screen.png")}\n<h3>Toolbar</h3>\n<p>In der Toolbar kannst du folgendes tun:\n<ul>\n    <li>Eine Kurswahl öffnen</li>\n    <li>Deine Kurswahl speichern</li>\n    <li>Die aktuelle Kurswahl zurücksetzen</li>\n    <li>Diese Hilfe öffnen</li>\n</ul>\n</p>\n<h3>Seitenleiste</h3>\n<p>\n    Die Seitenleiste dient der Navigation. Du bist vermutlich gerade auf der Übersicht, ganz unten. Um mit der Kurswahl\n    zu beginnen, klicke auf eine der dunkelgrauen Schaltflächen. Du kannst beliebig zwischen den Abschnitten wechseln,\n    solange deine Eingaben gültig sind. Solltest du etwas ändern, kann es sein, dass du ein paar Sachen in den \n    nachfolgenden Abschnitten neueingeben musst!\n</p>\n<h3>Unterer Fensterrand</h3>\n<p>Ganz links kannst du <b>die Schule wechseln</b>. In der Mitte wird dir angezeigt, wenn deine <b>Eingaben ungültig</b> sind. Ganz rechts kannst du <b>deine Wahl zurücksetzen.</b></p>\n\n<h2><a name='ablauf'>Ablauf</a></h2>\n<ol>\n    <li>Persönliche Daten eingeben (Seitenleiste)</li>\n    <li>Fremdsprachen, Wahlpflichtfächer und die Klasse auswählen (Seitenleiste)</li>\n    <li>Leistungskurse wählen (Seitenleiste)</li>\n    <li>Prüfungsfächer &AMP; 5. PK wählen (Seitenleiste)</li>\n    <li>Grundkurse wählen (Seitenleiste)</li>\n    <li>Datei oder Formular speichern (Toolbar)</li>\n</ol>\n<p>\n    Klicke auf den obersten Eintrag in der Seitenleiste um zu beginnen. Hast du deine Daten eingegeben, kannst du zum\n    nächsten Abschnitt übergehen. Einträge die noch ausgegraut sind, erfordern das Abschließen der vorherigen Abschnitte!\n</p>\n<h2><a name='speichern'>Speichern</a></h2>\n<p>\n    Um deine Wahl zu sichern, klicke auf das <b>Speichern-Symbol</b> (das in der Mitte) in der Toolbar. Je nach Schule\n    unterscheidet sich das weitere Vorgehen!\n</p>\n<h3>LUSD-Export</h3>\n<p>\n    Wirst du gefragt ein <b>LUSD-Formular</b> auszuwählen, dann öffne das Formular, welches du von deinem PäKo bekommen\n    hast. Wähle nun einen neuen Speicherort oder -Namen aus, um dein Formular zu speichern, ansonsten schlägt der Export\n    fehl!<br>\n    Möchtest du <b>nur eine Datei für dich</b> speichern, klicke direkt auf <b>Abbrechen</b>. Du wirst dann gefragt, ob du eine\n    <a href='#json_export'>Datei für dich exportieren</a> möchtest.\n</p>\n<h3><a name='json_export'>kurswahl-Export</a></h3>\n<p>\n    Wirst du nach einem Ort zum Speichern der <b>Datei für den PäKo gefragt</b>, nutzt deine Schule den Standard-Export.\n    Wähle einen Ort für die Datei, an dem du sie wiederfindest. Du kannst diese Datei auch später öffnen, um deine Wahl\n    zu ändern. Danach wirst du gefragt, einen Ort zum Speichern eines Bildes auszuwählen. Dort wird ein <b>Bild deiner\n    Wahl</b> gespeichert dass du ausdrucken und unterschreiben kannst.\n</p>\n<h2><a name='copyright'>Copyright</a></h2>\n&copy; 2022-${Calendar.getInstance().get(Calendar.YEAR)} Hendrik Horstmann. Zum <a href='https://github.com/heinrich26/Kurs-Check'>Quellcode</a>"

    override val windowName: String
        get() = R.getString("your_kurswahl")

    init {

        val visualizer = WahlVisualizer(wahlData)
        val scrollPane = JScrollPane(
            visualizer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
        scrollPane.verticalScrollBar.unitIncrement = 16
        scrollPane.viewportBorder = null
        scrollPane.border = null
        add(
            scrollPane,
            row = 0,
            column = 0,
            anchor = GridBagConstraints.CENTER,
            weighty = 1.0,
            weightx = 1.0
        )

        val prefHeight = visualizer.preferredSize.height
        val prefWidth = visualizer.preferredSize.width

        fun updateScrollpane() {
            scrollPane.preferredSize = if (height < prefHeight) Dimension(
                prefWidth + scrollPane.verticalScrollBar.preferredSize.width, height
            ) else Dimension(prefWidth + 16 /* Breite der Rounded Border */, prefHeight)
            revalidate()
        }


        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = updateScrollpane()
        })

        updateScrollpane()
    }
}