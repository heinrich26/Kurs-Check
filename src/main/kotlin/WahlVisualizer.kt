import data.KurswahlData
import gui.RoundedBorder
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator

class WahlVisualizer(val data: KurswahlData): JPanel() {
    init {
        layout = GridBagLayout()
        border = RoundedBorder(16)
        add(JLabel("Übersicht"), row = 0, column = 0, columnspan = 2)
        add(JSeparator(), column = 0, row = 1, columnspan = 2)

        if (data.lk1 != null) {
            add(JLabel("LK 1:"), row = 1, column = 0)
            add(JLabel(data.lk1!!.name), row = 1, column = 1, anchor = GridBagConstraints.WEST)
            if (data.lk2 != null) {
                add(JLabel("LK 2:"), row = 2, column = 0)
                add(JLabel(data.lk2!!.name), row = 2, column = 1, anchor = GridBagConstraints.WEST)
            }
        }
        if (data.pf3 != null) {
            add(JLabel("PF 3:"), row = 3, column = 0)
            add(JLabel(data.pf3!!.name), row = 3, column = 1, anchor = GridBagConstraints.WEST)
            if (data.pf4 != null) {
                add(JLabel("PF 4:"), row = 4, column = 0)
                add(JLabel(data.pf4!!.name), row = 4, column = 1, anchor = GridBagConstraints.WEST)
                if (data.pf5!= null) {
                    add(JLabel("PF 5:"), row = 5, column = 0)
                    add(JLabel(data.pf5!!.name), row = 5, column = 1, anchor = GridBagConstraints.WEST)
                }
            }
        }
    }
}