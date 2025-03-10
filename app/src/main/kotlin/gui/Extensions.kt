/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
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

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.io.File
import javax.swing.JComboBox
import javax.swing.JComponent

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
else this.wrapTags(*tags.drop(1).toTypedArray()).wrapHtml(tags[0])

/** Wrapped einen String in `<b>...</b>` */
fun String.bold() = "<b>$this</b>"

/** Wrapped einen String in `<i>...</i>` */
fun String.italic() = "<i>$this</i>"

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

operator fun Insets.component1() = top
operator fun Insets.component2() = left
operator fun Insets.component3() = bottom
operator fun Insets.component4() = right

@Suppress("SSBasedInspection")
infix fun Int.by(y: Int): Dimension = Dimension(this, y)
operator fun Dimension.component1() = width
operator fun Dimension.component2() = height

fun Path2D.scale(sx: Double, sy: Double): Shape = createTransformedShape(AffineTransform.getScaleInstance(sx, sy))

fun File.withExtension(ext: String): File = File(this.parentFile, "$nameWithoutExtension.$ext")

fun Color.hexString() = "#${Integer.toHexString(rgb and 0x00ffffff)}"

@Suppress("UNCHECKED_CAST", "UsePropertyAccessSyntax")
val <E> JComboBox<E>.selectedItem: E?
    get() = this.getSelectedItem() as E?

