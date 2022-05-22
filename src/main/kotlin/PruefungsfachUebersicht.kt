import javax.swing.JFrame
import javax.swing.JTable
import javax.swing.JScrollPane

class PruefungsfachUebersicht internal constructor() {
    var f: JFrame = JFrame()
    var j: JTable

    init {
        f.title = "static Window"
        val data = arrayOf(arrayOf("1.LK", "Informatik"), arrayOf("2.LK", "Mathe"), arrayOf("3.PF", "Englisch"), arrayOf("4.PF", "Geschichte"), arrayOf("5.PK", "Biologie"))
        val columnNames = arrayOf("Pfrüfungskomponente", "Fach")
        j = JTable(data, columnNames)
        j.setBounds(30, 30, 75, 150)
        j.disable()
        j.columnSelectionAllowed = false
        j.tableHeader.reorderingAllowed = false
        val sp = JScrollPane(j)
        f.add(sp)
        f.setSize(275, 700)
        f.isVisible = true
    }
}