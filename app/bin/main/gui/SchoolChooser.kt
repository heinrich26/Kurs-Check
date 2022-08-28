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

import java.awt.Component
import javax.swing.*
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE

fun chooseSchool(currentSchool: School?, parent: Component): School? {

    val listModel = DefaultListModel<School>()
    listModel.addAll(SchoolConfig.schools)
    val schoolList = JList(listModel)

    // Ausgangswert setzen
    if (currentSchool == null)
        schoolList.selectedIndex = 0
    else
        schoolList.selectedIndex = listModel.indexOf(currentSchool)

    schoolList.cellRenderer = SchoolRenderer()
    val pane = JScrollPane(schoolList)
    pane.setSize(200, 200)



    val optPane = JOptionPane(pane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null)
//    val dialog = optPane.createDialog(null as JDialog?, "Schule wählen!")
//
    val dialog = JDialog(null as JDialog?, "Schule wählen!", true)
    dialog.add(optPane)
    dialog.defaultCloseOperation = DISPOSE_ON_CLOSE
    dialog.pack()
    dialog.isVisible = true
    val result = optPane.value

    // TODO Dialog Zentrieren

//    val result = JOptionPane.showConfirmDialog(
//        null as JDialog?, pane, "Schule wählen!", JOptionPane.OK_CANCEL_OPTION,
//        JOptionPane.PLAIN_MESSAGE, null
//    )
    return if (result == JOptionPane.OK_OPTION) schoolList.selectedValue else null
}