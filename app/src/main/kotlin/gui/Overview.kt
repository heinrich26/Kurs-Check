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
import java.awt.GridBagConstraints
import java.awt.Insets
import java.net.URL
import javax.swing.JButton
import javax.swing.JPanel


class Overview(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData = wahlData

    override fun isDataValid(): Boolean = true // TODO Finales Checking?

    override val windowName: String
        get() = "Deine Kurswahl"

    init {
        add(JPanel(), weightx = 1.0, weighty = 1.0)
        add(WahlVisualizer(wahlData), row = 0, column = 0, anchor = GridBagConstraints.CENTER)
        add(
            JButton("\u24b8 Hendrik Horstmann").apply {
                isFocusable = false
                addActionListener { openWebpage(URL("https://github.com/heinrich26/Kurs-Check")) }
            }, row = 0, column = 0,
            anchor = GridBagConstraints.SOUTHEAST,
            margin = Insets(4, 4, 4, 4)
        )
    }

    /**
     * Öffnet eine Webseite im Browser
     */
    fun openWebpage(url: URL): Boolean {
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