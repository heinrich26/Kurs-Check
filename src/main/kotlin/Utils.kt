import data.Wahlmoeglichkeit
import java.awt.*
import java.lang.IllegalArgumentException

/**
 * Gibt den Inhalt der angeforderten Ressource zurück
 * @param fileName Name/Pfad der Datei
 */
fun getResource(fileName: String): String? = {}.javaClass.getResource(fileName)?.readText()

//enum class FILLS(val value: Int) { NONE(0), BOTH(1), HORIZONTAL(2), VERTICAL(3) }

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


/**
 * Fügt den [Component] zu dem [Container] mit [GridBagLayout] hinzu
 * und wendet die gegebenen Layoutparameter an
 */
fun Container.add(
    component: Component,
    column: Int = GridBagConstraints.RELATIVE,
    row: Int = GridBagConstraints.RELATIVE,
    columnspan: Int = 1,
    rowspan: Int = 1,
    weightx: Double = 0.0,
    weighty: Double = 0.0,
    anchor: Int = GridBagConstraints.CENTER,
    fill: Int = GridBagConstraints.NONE,
    margin: Insets = Insets(0, 0, 0, 0),
    ipadx: Int = 0,
    ipady: Int = 0
): Unit {
    /* if (this.layout !is GridBagLayout?)
        println(this.layout.javaClass.name)
        throw IllegalArgumentException("Der Component muss ein GridBagLayout besitzen") */

    this.add(component,
        GridBagConstraints(column, row, columnspan, rowspan,
            weightx, weighty, anchor, fill, margin, ipadx, ipady))
}