import data.Fach
import javax.swing.ComboBoxModel
import javax.swing.JComboBox
import javax.swing.event.ListDataListener

class ExclusiveComboBoxModel(val data: List<Fach>, private val vorgaenger: Array<JComboBox<Fach>>): ComboBoxModel<Fach> {
    override fun getSize(): Int = data.size - vorgaenger.size

    private val listeners = mutableListOf<ListDataListener>()

    private var selectedItem = data[0]


    override fun getElementAt(index: Int): Fach {
        var newIndex = index
        for (i in vorgaenger.map { it.selectedIndex })
            if (i <= index) newIndex++

        return data[newIndex]
    }

    override fun addListDataListener(l: ListDataListener?) {
        if (l != null) listeners.add(l)
    }

    override fun removeListDataListener(l: ListDataListener?) {
        if (l != null) listeners.remove(l)
    }

    override fun setSelectedItem(anItem: Any?) {
        selectedItem = anItem as Fach
    }

    override fun getSelectedItem(): Fach = selectedItem
}