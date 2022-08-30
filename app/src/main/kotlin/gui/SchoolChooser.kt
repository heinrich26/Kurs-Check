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

import com.kurswahlApp.data.School
import com.kurswahlApp.data.SchoolConfig
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import javax.swing.border.EmptyBorder
import kotlin.system.exitProcess

/**
 * Helfer Konstante um festzustellen, ob der Dialog über den 'Close'-Button geschlossen wurde!
 */
private const val CLOSE_OPTION = -1


/**
 * Dialog um eine Schule auszuwählen
 * @return neuausgewählte Schule oder `null`
 */
fun chooseSchool(currentSchool: School?): School? {

    val listModel = DefaultListModel<School>()
    listModel.addAll(SchoolConfig.schools)
    val schoolList = JList(listModel)

    // Ausgangswert setzen
    schoolList.selectedIndex =
        if (currentSchool == null) 0
        else listModel.indexOf(currentSchool)

    schoolList.cellRenderer = SchoolRenderer()
    val pane = JScrollPane(schoolList)
    pane.preferredSize = Dimension(242, 298)

    val layout = JPanel(GridBagLayout())
    layout.add(pane, row = 0, column = 0, columnspan = 2, weightx = 1.0)

    val dialog = JDialog(null as JDialog?, "Schule wählen!", true)
    var result = -1

    val okBtn = JButton("OK")
    val btnDim = Dimension(85, 25)
    okBtn.preferredSize = btnDim
    okBtn.addActionListener {
        result = JOptionPane.OK_OPTION
        dialog.dispose()
    }

    if (currentSchool != null) {
        layout.add(
            okBtn,
            row = 1,
            column = 0,
            anchor = GridBagConstraints.EAST,
            margin = Insets(9, 0, 2, 0),
            weightx = 1.0
        )
        layout.add(JButton("Abbrechen").apply {
            this.preferredSize = btnDim
            this.addActionListener {
                result = JOptionPane.CANCEL_OPTION
                dialog.dispose()
            }
        }, row = 1, column = 1, anchor = GridBagConstraints.WEST, margin = Insets(9, 0, 2, 0), weightx = 1.0)
    } else layout.add(okBtn, row = 1, column = 0, columnspan = 2, margin = Insets(9, 0, 2, 0))

    layout.border = EmptyBorder(10, 10, 10, 10)

    dialog.add(layout)
    dialog.isResizable = false
    dialog.defaultCloseOperation = DISPOSE_ON_CLOSE // TODO donotingonclose, wenn initial
    dialog.iconImages = GuiMain.MappedIcons
    dialog.pack()
    dialog.setLocationRelativeTo(null)
    dialog.isVisible = true

    // Dialog ausführen, warten auf Nutzereingabe...

    return when (result) {
        JOptionPane.OK_OPTION -> schoolList.selectedValue
        CLOSE_OPTION -> if (currentSchool == null) exitProcess(0) else null
        else -> null
    }
}