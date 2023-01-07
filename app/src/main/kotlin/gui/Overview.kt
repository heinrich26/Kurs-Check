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

import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import java.awt.Desktop
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.net.URL
import javax.swing.JButton
import javax.swing.JScrollPane


class Overview(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData = wahlData

    override fun isDataValid(): Boolean = true // TODO Finales Checking?

    override val windowName: String
        get() = "Deine Kurswahl"

    init {
        val copyrightButton = JButton("\u24b8 Hendrik Horstmann").apply {
            isFocusable = false
            addActionListener { openWebpage(URL("https://github.com/heinrich26/Kurs-Check")) }
        }
        add(
            copyrightButton,
            row = 2,
            column = 0,
            anchor = GridBagConstraints.SOUTHEAST,
            margin = Insets(4)
        )

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
        addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent) {
                val givenHeight = height - copyrightButton.height - 8 /* Insets des Buttons */
                scrollPane.preferredSize = if (givenHeight < prefHeight) Dimension(
                    prefWidth + scrollPane.verticalScrollBar.preferredSize.width,
                    givenHeight
                ) else Dimension(prefWidth + 16 /* Breite der Rounded Border */, prefHeight)
            }

            override fun componentMoved(e: ComponentEvent) {}

            override fun componentShown(e: ComponentEvent) {}

            override fun componentHidden(e: ComponentEvent) {}
        })
    }

    /**
     * Öffnet eine Webseite im Browser
     */
    private fun openWebpage(url: URL): Boolean {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI())
                return true
            } catch (_: Exception) {
            }
        }
        return false
    }
}