package gui

import data.Fach
import javax.swing.ComboBoxModel
import javax.swing.JComboBox
import kotlin.math.max

class FachComboBox(model: ComboBoxModel<Fach?>) : JComboBox<Fach?>(model) {
    override fun getSelectedIndex(): Int = max(super.getSelectedIndex(), 0)

    override fun getSelectedItem(): Fach? {
        return super.getSelectedItem() as Fach?
    }

    override fun getModel(): ComboBoxModel<Fach?> {
        return super.getModel() as ComboBoxModel<Fach?>
    }
}