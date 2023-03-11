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

import com.kurswahlApp.gui.VerticalLayout.Companion.BOTH
import com.kurswahlApp.gui.VerticalLayout.Companion.BOTTOM
import com.kurswahlApp.gui.VerticalLayout.Companion.CENTER
import com.kurswahlApp.gui.VerticalLayout.Companion.LEFT
import com.kurswahlApp.gui.VerticalLayout.Companion.RIGHT
import com.kurswahlApp.gui.VerticalLayout.Companion.TOP
import java.awt.*
import kotlin.math.max

/**
 *
 * A vertical layout manager similar to [java.awt.FlowLayout].
 * Like [FlowLayout] components do not expand to fill available space except when the horizontal alignment
 * is [BOTH]
 * in which case components are stretched horizontally. Unlike [FlowLayout], components will not wrap to form another
 * column if there isn't enough space vertically. [VerticalLayout] can optionally anchor components to the top or bottom
 * of the display area or center them between the top and bottom.
 *
 * Revision date 12th July 2001
 *
 * @author Colin Mummery  e-mail: colin_mummery@yahoo.com Homepage:www.kagi.com/equitysoft -
 * Based on `FlexLayout` in Java class libraries Vol 2 Chan/Lee Addison-Wesley 1998
 *
 * @property vgap An int value indicating the vertical seperation of the components
 * @property alignment An int value which is one of [RIGHT], [LEFT], [CENTER], [BOTH] for the horizontal alignment.
 * @property anchor An int value which is one of [TOP], [BOTTOM], [CENTER] indicating where the components are
 * to appear if the display area exceeds the minimum necessary.
 */
class VerticalLayout(
    private val vgap: Int = 5,
    private val alignment: Int = CENTER,
    private val anchor: Int = TOP
) : LayoutManager {

    private fun layoutSize(parent: Container): Dimension {
        val dim = Dimension(0, 0)
        var d: Dimension
        synchronized(parent.treeLock) {
            val n = parent.componentCount
            for (i in 0 until n) {
                val c = parent.getComponent(i)
                if (c.isVisible) {
                    d = c.preferredSize
                    dim.width = max(dim.width, d.width)
                    dim.height += d.height
                    if (i > 0) dim.height += vgap
                }
            }
        }
        val insets = parent.insets
        dim.width += insets.left + insets.right
        dim.height += insets.top + insets.bottom + vgap + vgap
        return dim
    }

    /**
     * Lays out the container.
     */
    override fun layoutContainer(parent: Container) {
        val insets = parent.insets
        synchronized(parent.treeLock) {
            val n = parent.componentCount
            val pd = parent.size
            var y = 0
            //work out the total size
            for (i in 0 until n) {
                val c = parent.getComponent(i)
                val d = c.preferredSize
                y += d.height + vgap
            }
            y -= vgap //otherwise there's a vgap too many
            //Work out the anchor paint
            y =
                if (anchor == TOP) insets.top else if (anchor == CENTER) (pd.height - y) / 2 else pd.height - y - insets.bottom
            //do layout
            for (i in 0 until n) {
                val c = parent.getComponent(i)
                val d = c.preferredSize
                var x = insets.left
                var wid = d.width
                when (alignment) {
                    CENTER -> x =
                        (pd.width - d.width) / 2
                    RIGHT -> x =
                        pd.width - d.width - insets.right
                    BOTH -> wid =
                        pd.width - insets.left - insets.right
                }
                c.setBounds(x, y, wid, d.height)
                y += d.height + vgap
            }
        }
    }


    override fun minimumLayoutSize(parent: Container): Dimension = layoutSize(parent)

    override fun preferredLayoutSize(parent: Container): Dimension = layoutSize(parent)

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
         * The horizontal alignment constant that designates stretching the component horizontally.
         */
        const val BOTH = 3

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
