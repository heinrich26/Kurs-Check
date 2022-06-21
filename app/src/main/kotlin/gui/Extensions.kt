package gui

import java.awt.*

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

/**
 * Macht einen [String] *wrappable*, sodass er sich an die Breite des Parent-[Component] anpasst.
 * Optionale [width], bei der ein Umbruch erzwugen wird
 */
fun String.wrappable(width: Int? = null) =
    if (width == null) "<html>$this</html>" else "<html><div style=\"width:${width}px;\">$this</div></html>"

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