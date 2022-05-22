package gui

import java.awt.Color
import java.awt.Polygon

object Consts {
    @JvmStatic
    val COLOR_PRIMARY = Color(96, 2, 238)

    @JvmStatic
    val COLOR_CONTROL = Color(COLOR_PRIMARY.colorSpace, COLOR_PRIMARY.getRGBColorComponents(FloatArray(3)), .4f)

    @JvmStatic
    val COLOR_ON_BACKGROUND: Color = Color.decode("#5E6367")

    @JvmStatic
    val COLOR_BACKGROUND: Color = Color.WHITE

    const val FONT_NAME = "Roboto"

    const val SIDEBAR_SIZE = 72

    val HOME_POLY = Polygon(intArrayOf(12, 20, 18, 18, 15, 15, 9, 9, 6, 6, 4), intArrayOf(4, 11, 11, 19, 19, 13, 13, 19, 19, 11, 11),11)
}
