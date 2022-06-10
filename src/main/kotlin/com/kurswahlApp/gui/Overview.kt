package com.kurswahlApp.gui

import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import java.awt.GridBagLayout

class Overview(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {
    override fun close(): KurswahlData = wahlData

    override fun isDataValid(): Boolean = true // TODO Finales Checking???

    override val windowName: String
        get() = "Deine Kurswahl"

    init {
        this.layout = GridBagLayout()

        add(WahlVisualizer(wahlData))
    }

}