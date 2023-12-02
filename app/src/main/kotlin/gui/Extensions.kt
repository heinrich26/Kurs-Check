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

package com.kurswahlApp.gui

import com.kurswahlApp.getResourceURL
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.io.File
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JComboBox
import javax.swing.JComponent
import kotlin.system.measureNanoTime

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
        component, GridBagConstraints(
            column, row, columnspan, rowspan, weightx, weighty, anchor, fill, margin, ipadx, ipady
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
fun String.wrapHtml(tag: String = "html", vararg styles: String): String = if (styles.isEmpty()) "<$tag>$this</$tag>"
else "<$tag style=\"${styles.joinToString(";", postfix = ";")}\">$this</$tag>"

/**
 * Fügt alle Html-[tags] von außen nach innen an den Enden an
 */
fun String.wrapTags(vararg tags: String): String = if (tags.isEmpty()) this
else if (tags.size == 1) this.wrapHtml(tags[0])
else this.wrapTags(*tags.takeLast(tags.size - 1).toTypedArray()).wrapHtml(tags[0])

fun JComponent.addMouseListener(
    onClick: (e: MouseEvent) -> Unit = {},
    onPress: (e: MouseEvent) -> Unit = {},
    onRelease: (e: MouseEvent) -> Unit = {},
    onEnter: (e: MouseEvent) -> Unit = {},
    onExit: (e: MouseEvent) -> Unit = {}
) {
    this.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) = onClick(e)
        override fun mousePressed(e: MouseEvent) = onPress(e)
        override fun mouseReleased(e: MouseEvent) = onRelease(e)
        override fun mouseEntered(e: MouseEvent) = onEnter(e)
        override fun mouseExited(e: MouseEvent) = onExit(e)

        // Hier könnten noch weitere Methoden von [MouseAdapter] stehen
    })
}

fun List<Boolean>.intersects(other: List<Boolean>): Boolean {
    for ((a, b) in this.zip(other)) if (a && b) return true
    return false
}

/**
 * Creates and initializes a new `Insets` object with the
 * specified inset on each side.
 * @param       all the inset from each side.
 */
fun Insets(all: Int) = Insets(all, all, all, all)

/**
 * Creates and initializes a new `Insets` object with the
 * specified x and y insets.
 * @param       x   the inset from the left and right.
 * @param       y   the inset from the top and bottom.
 */
fun Insets(x: Int = 0, y: Int = 0) = Insets(y, x, y, x)

/**
 * Creates and initializes a new `Insets` object with the
 * specified top, left, bottom, and right insets.
 * @param       top   the inset from the top.
 * @param       left   the inset from the left.
 * @param       bottom   the inset from the bottom.
 * @param       right   the inset from the right.
 */
fun Insets(top: Int = 0, left: Int = 0, bottom: Int = 0, right: Int = 0) = java.awt.Insets(top, left, bottom, right)

fun Path2D.scale(sx: Double, sy: Double): Shape = createTransformedShape(AffineTransform.getScaleInstance(sx, sy))

fun <R> measureNanos(block: () -> R): R {
    val result: R
    println(measureNanoTime { result = block() })
    return result
}

fun File.withExtension(ext: String): File = File(this.parentFile, "$nameWithoutExtension.$ext")

/**
 * Öffnet eine Webseite im Browser
 */
fun openWebpage(url: URL): Boolean {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(url.toURI())
            return true
        } catch (_: Exception) {
        }
    }
    return false
}

fun Color.hexString() = "#${Integer.toHexString(rgb and 0x00ffffff)}"

fun img(src: String, alt: String? = null) =
    if (alt != null) "<img src='${getResourceURL(src)}' alt='$alt'/>" else "<img src='${getResourceURL(src)}'/>"

fun img(src: String, width: Int, height: Int, alt: String? = null) =
    if (alt != null)
        "<img src='${getResourceURL(src)}' alt='$alt' width='$width' height='$height'/>"
    else
        "<img src='${getResourceURL(src)}' width='$width' height='$height'/>"

@Suppress("UNCHECKED_CAST")
val <E> JComboBox<E>.selectedItem: E?
    get() = this.getSelectedItem() as E?

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