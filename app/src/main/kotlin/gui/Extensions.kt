/*
 * Copyright (c) 2022  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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