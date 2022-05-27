package gui

import java.awt.Color
import java.awt.Polygon

object Consts {

    val COLOR_PRIMARY = Color(96, 2, 238)
    val COLOR_CONTROL = Color(COLOR_PRIMARY.colorSpace, COLOR_PRIMARY.getRGBColorComponents(FloatArray(3)), .1f)
    val COLOR_ON_BACKGROUND = Color(94, 99, 103)
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
    val HOME_POLY = Polygon(intArrayOf(12, 20, 18, 18, 15, 15, 9, 9, 6, 6, 4), intArrayOf(4, 11, 11, 19, 19, 13, 13, 19, 19, 11, 11),11)

    // Data URL Graphics
    const val CROSS_IMG = """data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAC4jAAAuIwF4pT92AAABT0lEQVQ4y4WRsUrDYBCAv/srUlsUwcHJNyjpKLhXFBEEJ0GtOoiIujiIWycfQERFUFrr6Cwi6OggFWnN6AuIi6jUSpOcQyXSNE1uvP++7/67k6/M3HDC9G6i8tL3XCwBSkQUwOxYiyuojDSbui8NK38lIpMAqhwma8WNCIn8ZPOnIMuter00AoP+q7DesJYOAImDWxmGjON5W6AfMZJOGH130W2TtssVV73xCEko7LheLl09f/K71K2F0YSYG5ABv0z1SIRkKGyXKwRnDZO0RztM2LK6SzphABMUpGrlB4W7jt7KbdouPwbzJuxUgsx03FBktpHNHwd/beJOhfL5P6+sBiUmCnZcL+d6zkSUJPrOfwurZ+bHEqbnGqHfr0BPktXSmnxb+T0jshu37TAJ6hWMgek4GCBlX9wHx0HMlFGhiKqq8toNDkoU3lA8hbNfHFzT0uoMaowAAAAASUVORK5CYII="""
}
