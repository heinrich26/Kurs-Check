import data.Fach
import javax.swing.ComboBoxModel
import javax.swing.JComboBox
import javax.swing.event.ListDataListener

class ExclusiveComboBoxModel(var data: List<Fach>, private val vorgaenger: JComboBox<Fach>? = null): ComboBoxModel<Fach> {
    override fun getSize(): Int = data.size + 1

    private val listeners = mutableListOf<ListDataListener>()

    private var selectedItem: Fach? = null

    private val itemsAfter: List<Fach>
        get() {
            val vorgaengerData = (vorgaenger!!.model as ExclusiveComboBoxModel).data
            return when (val i = vorgaenger.selectedIndex) {
                0 -> vorgaengerData.toList()
                1 -> vorgaengerData.subList(1, vorgaengerData.size)
                vorgaengerData.size -> vorgaengerData.subList(0, vorgaengerData.size - 1)
                else -> vorgaengerData.subList(0, i - 1) + vorgaengerData.subList(i, vorgaengerData.size)
            }
        }

    fun updateData() {
        ((vorgaenger?.model ?: return) as ExclusiveComboBoxModel).updateData()
        data = itemsAfter
        if (selectedItem !in data || vorgaenger.selectedIndex == 0) selectedItem = null
    }

    override fun getElementAt(index: Int): Fach? {
        if (index == 0) return null

        return data[index - 1]
    }

    override fun addListDataListener(l: ListDataListener?) {
        if (l != null) listeners.add(l)
    }

    override fun removeListDataListener(l: ListDataListener?) {
        if (l != null) listeners.remove(l)
    }

    override fun setSelectedItem(anItem: Any?) {
        selectedItem = anItem as Fach?
    }

    override fun getSelectedItem(): Fach? = selectedItem
}