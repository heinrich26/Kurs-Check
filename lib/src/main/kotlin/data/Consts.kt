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

@file:Suppress("unused")

package com.kurswahlApp.data

import java.awt.*
import java.awt.geom.*
import javax.swing.JLabel

object Consts {

    val COLOR_PRIMARY = Color(96, 2, 238)
    val COLOR_PRIMARY_FOCUS = COLOR_PRIMARY.brighter()
    val COLOR_ON_PRIMARY: Color = Color.WHITE
    val COLOR_ON_PRIMARY_FOCUS = Color(190, 190, 190)
    val COLOR_CONTROL = COLOR_PRIMARY.transparentise(.1f)
    val COLOR_ON_BACKGROUND = Color(94, 99, 103)
    val COLOR_ON_BACKGROUND_DISABLED = COLOR_ON_BACKGROUND.transparentise(.6f)
    val COLOR_BACKGROUND: Color = Color.WHITE
    val COLOR_VALID = Color(0, 210, 106)
    val COLOR_ERROR = Color(249, 47, 96)

    const val FONT_NAME = "Fira Sans"


    const val PANEL_WIDTH = 400
    const val PANEL_HEIGHT = 300

    const val SIDEBAR_SIZE = 72

    const val TOOLBAR_ICON_SIZE = 36

    // Checkbox Consts
    const val CHECKBOX_WIDTH = 56
    const val CHECKBOX_HEIGHT = 40

    // Poly Graphics
    val HOME_POLY = Polygon(
        intArrayOf(12, 20, 18, 18, 15, 15, 9, 9, 6, 6, 4), intArrayOf(4, 11, 11, 19, 19, 13, 13, 19, 19, 11, 11), 11
    )

    val PERSON_ICON: Shape = GeneralPath().apply {
        append(Ellipse2D.Float(12.5f, 4f, 11.5f, 11.5f), false)
        moveTo(6.05, 32.0)
        lineTo(29.95, 32.0)
        curveTo(29.95, 27.9, 26.2, 23.6, 21.5, 23.6)
        lineTo(14.5, 23.6)
        curveTo(9.8, 23.6, 6.05, 27.9, 6.05, 32.0)
        closePath()
    }.createTransformedShape(AffineTransform(20 / 36.0, .0, .0, 20 / 36.0, 2.0, 2.0))

    val CHECKBOX_CHECKED = Area(RoundRectangle2D.Float(0f, 0f, 24f, 24f, 10f, 10f)).apply {
        subtract(Area(GeneralPath().apply {
            moveTo(10.2, 17.4)
            lineTo(18.6, 9.0)
            lineTo(17.4, 7.8)
            lineTo(10.2, 15.0)
            lineTo(6.6, 11.4)
            lineTo(5.4, 12.6)
            closePath()
        }))
    }

    val CHECKBOX_LK: Area
    val CHECKBOX_PF3: Area
    val CHECKBOX_PF4: Area
    val CHECKBOX_PF5: Area

    init {
        val f = Font(FONT_NAME, Font.PLAIN, 16)
        with(JLabel().getFontMetrics(f).fontRenderContext) {
            val base = Area(RoundRectangle2D.Double(.0, .0, 24.0, 24.0, 10.0, 10.0))

            CHECKBOX_LK = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "LK")
                subtract(
                    Area(
                        vector.getOutline(
                            ((24 - vector.logicalBounds.width) / 2.0).toFloat(),
                            ((24 + vector.visualBounds.height) / 2.0).toFloat()
                        )
                    )
                )
            }
            CHECKBOX_PF3 = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "3.")
                vector.visualBounds.let {
                    subtract(
                        Area(
                            vector.getOutline(
                                ((24 - it.width) / 2.0).toFloat(), ((24 + it.height) / 2.0).toFloat()
                            )
                        )
                    )
                }
            }

            CHECKBOX_PF4 = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "4.")
                vector.visualBounds.let {
                    subtract(
                        Area(
                            vector.getOutline(
                                ((24 - it.width) / 2.0).toFloat(), ((24 + it.height) / 2.0).toFloat()
                            )
                        )
                    )
                }
            }

            CHECKBOX_PF5 = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "5.")
                vector.visualBounds.let {
                    subtract(
                        Area(
                            vector.getOutline(
                                ((24 - it.width) / 2.0).toFloat(), ((24 + it.height) / 2.0).toFloat()
                            )
                        )
                    )
                }
            }

        }
    }

    val RENDERING_HINTS = mapOf(
        RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
        RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
        RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_PURE
    )

    /** Allgemeiner Name für diese Anwendung */
    const val APP_NAME = "Kurs-Check"
    val APP_ICONS = arrayOf(
        "icons/app_icon_12.png",
        "icons/app_icon_16.png",
        "icons/app_icon_24.png",
        "icons/app_icon_32.png",
        "icons/app_icon_48.png",
        "icons/app_icon_64.png"
    )

    const val FILETYPE_EXTENSION = "kurswahl"
    const val TEST_FILE_NAME = "res/test_wahl.kurswahl"

    /** Höchstalter, um für die Gymnasiale Oberstufe zugelassen zu werden */
    const val MAX_ALTER = 22

    /** Mindestalter, um für die Gymnasiale Oberstufe zugelassen zu werden */
    const val MIN_ALTER = 12


    /*
    Konstanten für den LUSD Import/Export
     */
    const val SCHUELER_ID_FIELD = "SchuelerId"
    const val JAHRGANG_ID_FIELD = "AbiturJahrgangId"

    const val PF_5_TYP_FIELD = "RadioGroupAcroFormField_Pruefungskomponente"
    const val PF_5_VAL_PRAES = "Praesentation_0"
    const val PF_5_VAL_BLL = "BLL_0"
}
