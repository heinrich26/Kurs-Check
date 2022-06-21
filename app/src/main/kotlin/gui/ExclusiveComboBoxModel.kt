package gui

import com.kurswahlApp.data.Fach
import javax.swing.ComboBoxModel
import javax.swing.event.ListDataListener

class ExclusiveComboBoxModel(var data: List<Fach>, private val nachfolger: FachComboBox? = null) :
    ComboBoxModel<Fach?> {

    override fun getSize(): Int = data.size + 1

    private val listeners = mutableListOf<ListDataListener>()

    private var selectedItem: Fach? = null

    override fun getElementAt(index: Int): Fach? = if (index == 0) null else data[index - 1]

    override fun addListDataListener(l: ListDataListener?) {
        if (l != null) listeners.add(l)
    }

    override fun removeListDataListener(l: ListDataListener?) {
        if (l != null) listeners.remove(l)
    }

    override fun setSelectedItem(anItem: Any?) {
        selectedItem = anItem as Fach?
        if (nachfolger != null)
            if (selectedItem != null) {
                (nachfolger.model as ExclusiveComboBoxModel).data = data.minus(selectedItem!!)
                if (nachfolger.selectedItem == selectedItem) nachfolger.selectedItem = null
            } else {
                (nachfolger.model as ExclusiveComboBoxModel).data = data
                nachfolger.selectedItem = null
            }
    }

    override fun getSelectedItem(): Fach? = selectedItem
}