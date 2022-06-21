package gui

import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData

class Overview(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    override fun close(): KurswahlData = wahlData

    override fun isDataValid(): Boolean = true // TODO Finales Checking?

    override val windowName: String
        get() = "Deine Kurswahl"

    init {
        add(WahlVisualizer(wahlData))
    }

}