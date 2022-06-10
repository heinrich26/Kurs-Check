
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import data.FachData
import gui.Consts.FILETYPE_EXTENSION
import java.awt.*
import java.io.File
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.filechooser.FileFilter


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
 * Macht einen [String] *wrappable*, sodass er sich an die Breite des Parent-[Component] anpasst.
 * Optionale [width], bei der ein Umbruch erzwugen wird
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

/** Erstellt ein [ImageIcon] mit dem gegebenen [path] und einer optionalen [description]. */
fun createImageIcon(path: String, description: String? = null): ImageIcon? {
    val imgURL: URL? = getResourceURL(path)
    return if (imgURL != null) {
        ImageIcon(imgURL, description)
    } else {
        System.err.println("Couldn't find file: $path")
        null
    }
}

object KurswahlFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension == FILETYPE_EXTENSION

    override fun getDescription(): String = "Kurswahl Dateien (.$FILETYPE_EXTENSION)"
}

object PngFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension == "png"

    override fun getDescription(): String = "Png Dateien (.png)"
}


/**
 * Fügt am Anfang und Ende ein Html-[tag] an!
 * Kann zusätzlich [styles] hinzufügen
 */
fun String.wrapHtml(tag: String = "html", vararg styles: String): String =
    if (styles.isEmpty()) "<$tag>$this</$tag>"
    else "<$tag style=\"${styles.joinToString(";", postfix = ";")}\">$this</$tag>"

/**
 * Fügt alle Html-[tags] von außen nach innen an den Enden an
 */
fun String.wrapTags(vararg tags: String): String =
    if (tags.isEmpty()) this
    else if (tags.size == 1) this.wrapHtml(tags[0])
    else this.wrapTags(*tags.takeLast(tags.size - 1).toTypedArray()).wrapHtml(tags[0])

/**
 * Returns a list of `null`s of the given type with the given [size].
 */
inline fun <reified T> listOfNulls(size: Int): List<T?> = arrayOfNulls<T?>(size).toList()