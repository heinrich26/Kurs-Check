package gui

import data.FachData
import data.KurswahlData
import javax.swing.JPanel

abstract class KurswahlPanel(val wahlData: KurswahlData, val fachData: FachData) : JPanel() {
    abstract fun close() : KurswahlData
}