package com.kurswahlApp.gui

import com.kurswahlApp.data.Fach
import javax.swing.DefaultComboBoxModel

open class FachComboBoxModel(protected var data: Collection<Fach>) :
    DefaultComboBoxModel<Fach?>(data.toTypedArray())