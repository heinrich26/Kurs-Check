package data

import javax.swing.DefaultComboBoxModel

open class FachComboBoxModel(protected var data: Collection<Fach>) :
    DefaultComboBoxModel<Fach?>(data.toTypedArray())