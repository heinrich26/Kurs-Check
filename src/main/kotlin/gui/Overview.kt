package gui

import data.FachData
import data.KurswahlData
import java.awt.GridBagLayout

class Overview(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {
    override fun close(): KurswahlData = wahlData

    override fun isDataValid(): Boolean = true // TODO Finales Checking???

    override val windowName: String
        get() = "Übersicht"

    init {
        this.layout = GridBagLayout()

        add(WahlVisualizer(wahlData))
    }

}