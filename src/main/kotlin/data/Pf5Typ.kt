package data

import com.fasterxml.jackson.annotation.JsonProperty
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

enum class Pf5Typ(val repr: String) {
    @JsonProperty("schriftl") SCHRIFTLICH("schriftlich"),
    @JsonProperty("praes") PRAESENTATION("Präsentation"),
    @JsonProperty("wettbewerb") WETTBEWERB("Wettbewerb");

    object Renderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component? = super.getListCellRendererComponent(
            list,
            if (value is Pf5Typ) value.repr else "Ungesetzt",
            index,
            isSelected,
            cellHasFocus
        )
    }
}