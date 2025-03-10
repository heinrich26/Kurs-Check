/*
 * Copyright (c) 2023-2025  Hendrik Horstmann
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

import com.kurswahlApp.data.Consts
import com.kurswahlApp.data.getResourceURL
import java.awt.Desktop
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.File
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.UIDefaults
import javax.swing.UIManager
import javax.swing.filechooser.FileFilter
import javax.swing.plaf.FontUIResource
import kotlin.system.measureNanoTime

fun prepareUI() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    arrayOf("Bold", "BoldItalic", "Italic", "Regular").forEach {
        ge.registerFont(
            Font.createFont(
                Font.TRUETYPE_FONT,
                getResourceURL("FiraSans-$it.ttf")!!.openStream()
            )
        )
    }

    val defaults = UIManager.getLookAndFeelDefaults()
    // Font Hack
    for ((key, value) in defaults) {
        if (key is String && key.endsWith(".font")) {
            // Hack für WindowsLookAndFeel
            if (value is UIDefaults.ActiveValue) {
                val val2 = value.createValue(defaults)
                if (val2 is FontUIResource) {
                    defaults[key] = FontUIResource(Consts.FONT_NAME, val2.style, 13)
                }
            } else if (value is FontUIResource) {
                // Hack für den Standard LookAndFeel
                defaults[key] = FontUIResource(Consts.FONT_NAME, value.style, 13)
            }
        }
    }
}

object KurswahlFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension == Consts.FILETYPE_EXTENSION

    override fun getDescription(): String = "Kurswahl Dateien (.${Consts.FILETYPE_EXTENSION})"
}

object PngFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || f.extension == "png"

    override fun getDescription(): String = "Png Dateien (.png)"
}

object PdfFileFilter : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory || (f.extension == "pdf" && (!f.exists() || f.canWrite()))

    override fun getDescription(): String = "LUSD Formulare (.pdf)"
}

class ExclusivePdfFileFilter(private val exclude: File) : FileFilter() {
    override fun accept(f: File): Boolean = f.isDirectory  || (f != exclude && f.extension == "pdf" && (!f.exists() || f.canWrite()))

    override fun getDescription(): String = "Pdf-Dateien (.pdf)"
}

fun <R> measureNanos(block: () -> R): R {
    val result: R
    println(measureNanoTime { result = block() })
    return result
}

/**
 * Öffnet eine Webseite im Browser
 */
fun openWebpage(url: URL): Boolean {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(url.toURI())
            return true
        } catch (_: Exception) {}
    }
    return false
}

fun img(src: String, alt: String? = null) =
    if (alt != null) "<img src='${getResourceURL(src)}' alt='$alt'/>" else "<img src='${getResourceURL(src)}'/>"

fun img(src: String, width: Int, height: Int, alt: String? = null) =
    if (alt != null)
        "<img src='${getResourceURL(src)}' alt='$alt' width='$width' height='$height'/>"
    else
        "<img src='${getResourceURL(src)}' width='$width' height='$height'/>"

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