import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import data.FachData
import java.awt.*
import java.net.URL
import javax.swing.ImageIcon


/**
 * Gibt den Inhalt der angeforderten Ressource zurück
 * @param fileName Name/Pfad der Datei
 */
fun getResource(fileName: String): String? = {}.javaClass.getResource(fileName)?.readText()

/**
 * Gibt die URL der angeforderten Ressource zurück
 * @param fileName Name/Pfad der Datei
 */
fun getResourceURL(fileName: String): URL? = {}.javaClass.getResource(fileName)

/**
 * Ließt die `dataStruct.json` als [FachData] Objekt ein
 */
fun readDataStruct(): FachData {
    val mapper = jacksonObjectMapper()
    mapper.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
    return mapper.readValue(getResourceURL("dataStruct.json")!!)
}

/**
 * Macht einen String "wrappable", sodass er sich an den Component anpasst.
 * @param width Optionale Länge, bei der ein Umbruch erzwugen wird
 */
fun String.wrappable(width: Int? = null) =
    if (width == null) "<html>$this</html>" else "<html><div style=\"width:${width}px;\">$this</div></html>"


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
) {
    if (this.layout !is GridBagLayout?) {
        println()
        throw IllegalArgumentException("Falsches Layout: ${this.layout.javaClass.name}! Der Component muss ein GridBagLayout besitzen")
    }

    this.add(
        component,
        GridBagConstraints(
            column, row, columnspan, rowspan,
            weightx, weighty, anchor, fill, margin, ipadx, ipady
        )
    )
}

/** Returns an ImageIcon, or null if the path was invalid. */
fun createImageIcon(path: String, description: String? = null): ImageIcon? {
    val imgURL: URL? = getResourceURL(path)
    return if (imgURL != null) {
        ImageIcon(imgURL, description)
    } else {
        System.err.println("Couldn't find file: $path")
        null
    }
}