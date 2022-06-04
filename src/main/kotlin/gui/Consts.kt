package gui

import java.awt.Color
import java.awt.Polygon
import java.awt.RenderingHints

object Consts {

    val COLOR_PRIMARY = Color(96, 2, 238)
    val COLOR_CONTROL = Color(COLOR_PRIMARY.colorSpace, COLOR_PRIMARY.getRGBColorComponents(null), .1f)
    val COLOR_ON_BACKGROUND = Color(94, 99, 103)
    val COLOR_ON_BACKGROUND_DISABLED =
        Color(COLOR_ON_BACKGROUND.colorSpace, COLOR_ON_BACKGROUND.getRGBColorComponents(null), .6f)
    val COLOR_BACKGROUND: Color = Color.WHITE
    val COLOR_VALID = Color(0, 210, 106)
    val COLOR_ERROR = Color(249, 47, 96)

    const val FONT_NAME = "Roboto"

    const val CHECK_CHAR = '\u2705'
    const val CROSS_CHAR = '\u274C'

    const val PANEL_WIDTH = 400
    const val PANEL_HEIGHT = 300

    const val SIDEBAR_SIZE = 72

    // Checkbox Consts
    const val CHECKBOX_WIDTH = 56
    const val CHECKBOX_HEIGHT = 40

    // Poly Graphics
    val HOME_POLY = Polygon(
        intArrayOf(12, 20, 18, 18, 15, 15, 9, 9, 6, 6, 4),
        intArrayOf(4, 11, 11, 19, 19, 13, 13, 19, 19, 11, 11),
        11
    )


    val RENDERING_HINTS = mapOf(
        RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
        RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
        RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_PURE
    )
}
