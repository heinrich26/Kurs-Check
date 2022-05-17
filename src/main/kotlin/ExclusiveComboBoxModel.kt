import data.Fach
import javax.swing.ComboBoxModel
import javax.swing.JComboBox
import javax.swing.event.ListDataListener

class ExclusiveComboBoxModel(val data: List<Fach?>, private val vorgaenger: Array<JComboBox<Fach?>>): ComboBoxModel<Fach?> {
    override fun getSize(): Int = data.size - vorgaenger.size + 1

    private val listeners = mutableListOf<ListDataListener>()

    private var selectedItem = data[0]


    override fun getElementAt(index: Int): Fach? {
        if (index == 0) return null

        var newIndex = index - 1
        println(vorgaenger.map { it.selectedIndex - 1 })
        for (i in vorgaenger.map { it.selectedIndex - 1 })
            if (i != -1 && i <= index) newIndex++

        return data[newIndex]
    }

    override fun addListDataListener(l: ListDataListener?) {
        if (l != null) listeners.add(l)
    }

    override fun removeListDataListener(l: ListDataListener?) {
        if (l != null) listeners.remove(l)
    }

    override fun setSelectedItem(anItem: Any?) {
        selectedItem = if (anItem is Fach) anItem else null
    }

    override fun getSelectedItem(): Fach? = selectedItem
}