package com.kurswahlApp.gui

import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent

open class ClickableDestionation(defaultSelected: Boolean = false, defaultEnabled: Boolean = true, clickEvent: () -> Unit) : JComponent() {
    var hasFocus = false
        set(value) {
            field = value
            repaint()
        }

    var isSelected = defaultSelected
        set(value) {
            field = value
            repaint()
        }

    init {
        Dimension(Consts.SIDEBAR_SIZE, Consts.SIDEBAR_SIZE).let {
            minimumSize = it
            preferredSize = it
        }

        isEnabled = defaultEnabled

        this.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                if (isEnabled && !isSelected) clickEvent()
            }

            override fun mousePressed(e: MouseEvent?) {}

            override fun mouseReleased(e: MouseEvent?) {}

            override fun mouseEntered(e: MouseEvent?) {
                hasFocus = isEnabled
            }

            override fun mouseExited(e: MouseEvent?) {
                hasFocus = false
            }
        })
    }
}
