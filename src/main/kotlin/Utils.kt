import java.awt.GridBagConstraints
import java.awt.Insets

/**
 * Gibt den Inhalt der angeforderten Ressource zurück
 * @param fileName Name/Pfad der Datei
 */
fun getResource(fileName: String): String? = {}.javaClass.getResource(fileName)?.readText()

class GridConstraint {
    companion object {
        fun create(
            column: Int = GridBagConstraints.RELATIVE,
            row: Int = GridBagConstraints.RELATIVE,
            columnspan: Int = 1,
            rowspan: Int = 1,
            weightx: Double = 0.0,
            weighty: Double = 0.0,
            anchor: Int = GridBagConstraints.CENTER,
            fill: Int = GridBagConstraints.NONE,
            insets: Insets = Insets(0, 0, 0, 0),
            ipadx: Int = 0,
            ipady: Int = 0
        ): GridBagConstraints {
            return GridBagConstraints(
                column,
                row,
                columnspan,
                rowspan,
                weightx,
                weighty,
                anchor,
                fill,
                insets,
                ipadx,
                ipady
            )
        }
    }
}