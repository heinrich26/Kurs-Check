package gui

import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent

open class ClickableDestionation(defaultEnabled: Boolean = false, clickEvent: () -> Unit) : JComponent() {
    var hasFocus = false
        set(value) {
            field = value
            repaint()
        }

    init {
        isEnabled = defaultEnabled

        Dimension(Consts.SIDEBAR_SIZE, Consts.SIDEBAR_SIZE).let {
            minimumSize = it
            preferredSize = it
        }

        this.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                if (!isEnabled) clickEvent()
            }

            override fun mousePressed(e: MouseEvent?) {}

            override fun mouseReleased(e: MouseEvent?) {}

            override fun mouseEntered(e: MouseEvent?) {
                hasFocus = true
            }

            override fun mouseExited(e: MouseEvent?) {
                hasFocus = false
            }
        })
    }
}
