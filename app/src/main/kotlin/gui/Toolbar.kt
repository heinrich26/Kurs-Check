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

package gui

import com.kurswahlApp.data.Consts.COLOR_ON_PRIMARY
import com.kurswahlApp.data.Consts.COLOR_ON_PRIMARY_FOCUS
import com.kurswahlApp.data.Consts.COLOR_PRIMARY
import com.kurswahlApp.data.Consts.RENDERING_HINTS
import com.kurswahlApp.data.Consts.TOOLBAR_ICON_SIZE
import java.awt.*
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder


/**
 * Aims to Implement a Material Toolbar with a Drop Shadow
 */
@Suppress("unused")
class Toolbar(text: String, private val shadowSize: Int = 8) : JPanel(GridBagLayout()) {
    var text: String
        get() = label.text
        set(value) {
            label.text = value
        }

    private val label = JLabel(text, SwingConstants.LEFT).apply {
        this.font = font.deriveFont(Font.BOLD, 20f)
        this.foreground = Color.WHITE
    }

    private val actionButtons: MutableList<ActionItem> = mutableListOf()


    init {
        isOpaque = false
        background = COLOR_PRIMARY
        border = EmptyBorder(16, 8, 16 + this.shadowSize, 8)

        add(label, weightx = 1.0, anchor = GridBagConstraints.WEST, fill = GridBagConstraints.HORIZONTAL)
    }


    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = background
        g.fillRect(x, y, width, height - this.shadowSize)

        with(g as Graphics2D) {
            val startY = y + height - shadowSize
            paint = GradientPaint(0f, startY.toFloat(), startColor, 0f, (startY + shadowSize).toFloat(), endColor)
            fillRect(x, startY, width, shadowSize)
        }
    }

    private var i = 0

    fun addActionItem(shape: Shape, id: String, tooltip: String? = null, action: () -> Unit) {
        val btn = ActionItem(shape, id, tooltip, action)
        actionButtons.add(btn)
        add(btn, row = 0, column = i++, anchor = GridBagConstraints.EAST, margin = Insets(0, 8, 0, 0))
    }

    fun removeActionItem(id: String) {
        val btn = actionButtons.find { it.id == id } ?: return
        actionButtons.remove(btn)
        remove(btn)
    }

    companion object {
        val startColor = Color(0, 0, 0, 80)
        val endColor = Color(0, 0, 0, 0)


        // ActionButton Stuff
        private class ActionItem(private val icon: Shape, val id: String, tooltip: String?, action: () -> Unit) : JComponent() {

            var hasFocus = false
                set(value) {
                    field = value
                    repaint()
                }

            init {
                preferredSize = Dimension(TOOLBAR_ICON_SIZE, TOOLBAR_ICON_SIZE)
                minimumSize = preferredSize

                toolTipText = tooltip

                addMouseListener(onClick = { action() }, onEnter = { hasFocus = true }, onExit = { hasFocus = false })
            }

            override fun paintComponent(g: Graphics) {
                val g2D = g as Graphics2D

                g2D.setRenderingHints(RENDERING_HINTS)

                g2D.color = if (hasFocus) COLOR_ON_PRIMARY_FOCUS else COLOR_ON_PRIMARY
                g2D.fill(icon)

                g.dispose()
            }
        }
    }
}