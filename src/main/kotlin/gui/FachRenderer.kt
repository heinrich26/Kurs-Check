package gui

import data.Fach
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList


object FachRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component? = super.getListCellRendererComponent(
        list,
        if (value is Fach) value.name else "Ungesetzt",
        index,
        isSelected,
        cellHasFocus
    )
}