@file:Suppress("unused")

package com.kurswahlApp.gui

import java.awt.*
import java.awt.geom.*
import javax.swing.JLabel

object Consts {

    val COLOR_PRIMARY = Color(96, 2, 238)
    val COLOR_ON_PRIMARY: Color = Color.WHITE
    val COLOR_ON_PRIMARY_FOCUS = Color(190, 190, 190)
    val COLOR_CONTROL = Color(COLOR_PRIMARY.colorSpace, COLOR_PRIMARY.getRGBColorComponents(null), .1f)
    val COLOR_ON_BACKGROUND = Color(94, 99, 103)
    val COLOR_ON_BACKGROUND_DISABLED =
        Color(COLOR_ON_BACKGROUND.colorSpace, COLOR_ON_BACKGROUND.getRGBColorComponents(null), .6f)
    val COLOR_BACKGROUND: Color = Color.WHITE
    val COLOR_VALID = Color(0, 210, 106)
    val COLOR_ERROR = Color(249, 47, 96)

    const val FONT_NAME = "Roboto"


    const val PANEL_WIDTH = 400
    const val PANEL_HEIGHT = 300

    const val SIDEBAR_SIZE = 72

    const val TOOLBAR_ICON_SIZE = 32

    // Checkbox Consts
    const val CHECKBOX_WIDTH = 56
    const val CHECKBOX_HEIGHT = 40

    // Poly Graphics
    val HOME_POLY = Polygon(
        intArrayOf(12, 20, 18, 18, 15, 15, 9, 9, 6, 6, 4),
        intArrayOf(4, 11, 11, 19, 19, 13, 13, 19, 19, 11, 11),
        11
    )

    val PERSON_ICON: Shape = GeneralPath().apply {
        append(Ellipse2D.Double(12.5, 4.0, 11.5, 11.5), false)
        moveTo(6.05, 32.0)
        lineTo(29.95, 32.0)
        curveTo(29.95, 27.9, 26.2, 23.6, 21.5, 23.6)
        lineTo(14.5, 23.6)
        curveTo(9.8, 23.6, 6.05, 27.9, 6.05, 32.0)
        closePath()
    }.createTransformedShape(AffineTransform(20/36.0, .0, .0, 20/36.0, 2.0, 2.0))

    val IMPORT_ICON = GeneralPath().apply {
        moveTo(8.25, .0)
        lineTo(16.5, .0)
        lineTo(16.5, 6.0)
        curveTo(16.5, 6.835, 17.165, 7.5, 18.0, 7.5)
        lineTo(24.0, 7.5)
        lineTo(24.0, 21.75)
        curveTo(24.0, 23.0, 23.0, 24.0, 21.75, 24.0)
        lineTo(8.25, 24.0)
        curveTo(7.0, 24.0, 6.0, 23.0, 6.0, 21.75)
        lineTo(6.0, 15.75)
        lineTo(14.161, 15.75)
        lineTo(12.33, 17.58)
        curveTo(12.12, 17.8, 12.0, 18.095, 12.0, 18.375)
        curveTo(12.0, 18.675, 12.12, 18.955, 12.331, 19.17)
        curveTo(12.78, 19.62, 13.485, 19.62, 13.922, 19.17)
        lineTo(17.672, 15.42)
        curveTo(18.11, 14.98, 18.11, 14.27, 17.672, 13.83)
        lineTo(13.922, 10.08)
        curveTo(13.49, 9.65, 12.77, 9.65, 12.331, 10.08)
        curveTo(11.9, 10.52, 11.9, 11.245, 12.331, 11.67)
        lineTo(14.161, 13.5)
        lineTo(6.0, 13.5)
        lineTo(6.0, 2.25)
        curveTo(6.0, 1.0, 7.0, .0, 8.25, .0)
        closePath()


        moveTo(6.0, 15.75)
        lineTo(6.0, 13.5)
        lineTo(1.125, 13.5)
        curveTo(.5, 13.5, .0, 14.0, .0, 14.625)
        curveTo(.0, 15.25, .5, 15.75, 1.125, 15.75)
        closePath()


        moveTo(18.0, .0)
        lineTo(18.0, 6.0)
        lineTo(24.0, 6.0)
        closePath()
    }

    val SAVE_ICON = Area(RoundRectangle2D.Double(.0, .0, 24.0, 24.0, 4.0, 4.0)).apply {
        subtract(Area(GeneralPath().apply {
            moveTo(18.5, .0)
            lineTo(24.0, .0)
            lineTo(24.0, 5.5)
            closePath()
        }))
        subtract(Area(Rectangle2D.Double(2.75, 2.75, 13.25, 5.5)))
        subtract(Area(Ellipse2D.Double(8.0, 13.5, 8.0, 8.0)))
        transform(AffineTransform.getScaleInstance(22 / 24.0, 22 / 24.0))
        transform(AffineTransform.getTranslateInstance(1.0, 1.0))
    }

    val CHECKBOX_CHECKED = Area(RoundRectangle2D.Double(.0, .0, 24.0, 24.0, 10.0, 10.0)).apply {
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
                subtract(Area(vector.getOutline(((24-vector.logicalBounds.width)/2.0).toFloat(), ((24+vector.visualBounds.height)/2.0).toFloat())))
            }
            CHECKBOX_PF3 = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "3.")
                vector.visualBounds.let {
                    subtract(Area(vector.getOutline(((24-it.width)/2.0).toFloat(), ((24+it.height)/2.0).toFloat())))
                }
            }

            CHECKBOX_PF4 = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "4.")
                vector.visualBounds.let {
                    subtract(Area(vector.getOutline(((24-it.width)/2.0).toFloat(), ((24+it.height)/2.0).toFloat())))
                }
            }

            CHECKBOX_PF5 = (base.clone() as Area).apply {
                val vector = f.createGlyphVector(this@with, "5.")
                vector.visualBounds.let {
                    subtract(Area(vector.getOutline(((24-it.width)/2.0).toFloat(), ((24+it.height)/2.0).toFloat())))
                }
            }

        }
    }

    const val APP_NAME = "Kurs-Check"
    val APP_ICONS = arrayOf("icons/app_icon_12.png", "icons/app_icon_16.png", "icons/app_icon_24.png", "icons/app_icon_32.png", "icons/app_icon_48.png", "icons/app_icon_64.png")


    val RENDERING_HINTS = mapOf(
        RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
        RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
        RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_PURE
    )

    const val FILETYPE_EXTENSION = "kurswahl"
    const val TEST_FILE_NAME = "test_wahl.kurswahl"

    /** Höchstalter um für die Gymnasiale Oberstufe zugelassen zu werden */
    const val MAX_ALTER = 22
    /** Mindestalter um für die Gymnasiale Oberstufe zugelassen zu werden */
    const val MIN_ALTER = 12
}
