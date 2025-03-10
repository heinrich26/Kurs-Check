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

import com.kurswahlApp.gui.VerticalLayout.Companion.BOTH
import com.kurswahlApp.gui.VerticalLayout.Companion.BOTTOM
import com.kurswahlApp.gui.VerticalLayout.Companion.CENTER
import com.kurswahlApp.gui.VerticalLayout.Companion.EQUAL
import com.kurswahlApp.gui.VerticalLayout.Companion.LEFT
import com.kurswahlApp.gui.VerticalLayout.Companion.RIGHT
import com.kurswahlApp.gui.VerticalLayout.Companion.TOP
import java.awt.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 *
 * A vertical layout manager similar to [java.awt.FlowLayout].
 * Like [FlowLayout] components do not expand to fill available space except
 * when the horizontal alignment is [BOTH] or [EQUAL] in which case components
 * are stretched horizontally. Unlike [FlowLayout]. Components will wrap to
 * form another column if there isn't enough space vertically.
 * [VerticalLayout] can optionally anchor components to the top or bottom
 * of the display area or center them between the top and bottom.
 *
 *
 * @property vgap An int value indicating the vertical seperation of the components
 * @property alignment An int value which is one of [RIGHT], [LEFT], [CENTER], [BOTH], [EQUAL] for the horizontal alignment.
 * @property anchor An int value which is one of [TOP], [BOTTOM], [CENTER] indicating where the components are
 * to appear if the display area exceeds the minimum necessary.
 */
class VerticalLayout(
    private val vgap: Int = 5,
    private val alignment: Int = CENTER,
    private val anchor: Int = TOP
) : LayoutManager {

    private fun moveComponents(
        target: Container, x: Int, y: Int, width: Int, height: Int,
        rowStart: Int, rowEnd: Int
    ): Int {
        var curY = y
        when (anchor) {
            TOP -> curY += 0
            CENTER -> curY += height / 2
            BOTTOM -> curY += height
        }
        for (m in (rowStart until rowEnd).map(target::getComponent).filter { it.isVisible }) {
            val cx: Int = x + (width - m.width) / 2

            m.setLocation(cx, curY)

            curY += m.height + vgap
        }
        return width
    }

    private fun columns(target: Container): Map<Int, Int> = buildMap {
        synchronized(target.treeLock) {
            val maxHeight = target.height - (target.insets.let { it.top + it.bottom })
            var x = 0
            val y0 = target.insets.top
            var y = y0
            var colW = 0
            var start = 0
            for ((i, m) in target.components.withIndex().filter { it.value.isVisible }) {
                val d = m.preferredSize
                if (y == y0 || y + d.height <= maxHeight) {
                    if (y > 0) {
                        y += vgap
                    }
                    y += d.height
                    colW = max(colW, d.width)
                } else {
                    put(start, colW)
                    start = i
                    x += vgap + colW
                    y = d.height
                    colW = d.width
                }
            }
            put(start, colW)
        }
    }

    override fun layoutContainer(target: Container) {
        synchronized(target.treeLock) {
            val (top, left, bottom, right) = target.insets
            var x = 0
            var y = top
            if (alignment == BOTH || alignment == EQUAL) {
                val columns = columns(target).let {
                    if (alignment == BOTH) {
                        val f = (target.size.width - left - right - (it.size - 1) * vgap).toDouble() / (it.values.sum())
                        it.mapValues { (_, v) -> (v * f).roundToInt() }
                    } else {
                        val w = (target.size.width - left - right - (it.size - 1) * vgap) / it.size
                        it.mapValues { w }
                    }
                }

                var colW = 0
                for ((i, c) in target.components.withIndex().filter { it.value.isVisible }) {
                    if (i in columns) {
                        x += colW
                        if (i != 0) x += vgap
                        y = top
                        colW = columns[i]!!
                    }
                    val prefHeight = c.preferredSize.height
                    c.setBounds(x, y, colW, prefHeight)
                    y += prefHeight + vgap
                }


                return
            }
            val maxheight = target.height - (top + bottom + vgap * 2)
            var colw = 0
            var start = 0
            for ((i, m) in target.components.withIndex().filter { it.value.isVisible }) {
                val d = m.preferredSize
                m.setSize(d.width, d.height)
                if (y == top || y + d.height <= maxheight) {
                    if (y > 0) {
                        y += vgap
                    }
                    y += d.height
                    colw = max(colw, d.width)
                } else {
                    colw = moveComponents(
                        target, x, top,
                        colw, maxheight - y, start, i
                    )
                    x += vgap + colw
                    y = d.height
                    colw = d.width
                    start = i
                }
            }
            moveComponents(
                target, x, top, colw, maxheight - y,
                start, target.componentCount
            )
        }
    }

    override fun minimumLayoutSize(target: Container): Dimension {
        synchronized(target.treeLock) {
            val dim = 0 by 0
            for (m in target.components.filter { it.isVisible }) {
                val d = m.minimumSize
                dim.width = max(dim.width, d.width)
                dim.height = max(dim.height, d.height)

            }
            val insets = target.insets
            dim.width += insets.left + insets.right
            dim.height += insets.top + insets.bottom + vgap * 2
            return dim
        }
    }

    override fun preferredLayoutSize(target: Container): Dimension {
        synchronized(target.treeLock) {
            val dim = 0 by 0
            var firstVisibleComponent = true
            for (m in target.components.filter { it.isVisible }) {
                val d = m.preferredSize
                dim.width = max(dim.width, d.width)
                if (firstVisibleComponent) {
                    firstVisibleComponent = false
                } else {
                    dim.height += vgap
                }
                dim.height += d.height
            }
            val insets = target.insets
            dim.width += insets.left + insets.right
            dim.height += insets.top + insets.bottom + vgap * 2
            return dim
        }
    }

    /**
     * Not used by this class
     */
    override fun addLayoutComponent(name: String, comp: Component) {}

    /**
     * Not used by this class
     */
    override fun removeLayoutComponent(comp: Component) {}


    override fun toString(): String = "${javaClass.name}[vgap=$vgap align=$alignment anchor=$anchor]"

    companion object {
        /**
         * The horizontal alignment constant that designates centering. Also used to designate center anchoring.
         */
        const val CENTER = 0

        /**
         * The horizontal alignment constant that designates right justification.
         */
        const val RIGHT = 1

        /**
         * The horizontal alignment constant that designates left justification.
         */
        const val LEFT = 2

        /**
         * The horizontal alignment constant that designates stretching
         * the components horizontally.
         */
        const val BOTH = 3

        /**
         * The horizontal alignment constant that designats the stretching of all
         * components equally along the horizontal axis.
         */
        const val EQUAL = 4

        /**
         * The anchoring constant that designates anchoring to the top of the display area
         */
        const val TOP = 1

        /**
         * The anchoring constant that designates anchoring to the bottom of the display area
         */
        const val BOTTOM = 2
    }
}
