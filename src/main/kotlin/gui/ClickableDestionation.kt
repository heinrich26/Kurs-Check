package gui

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent

class ClickableDestionation(val holder: JComponent, defaultEnabled: Boolean = false, activationEvent: () -> Unit) {
    init {
        holder.isEnabled = defaultEnabled

        holder.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                if (!holder.isEnabled) activationEvent()
            }

            override fun mousePressed(e: MouseEvent?) {}

            override fun mouseReleased(e: MouseEvent?) {}

            override fun mouseEntered(e: MouseEvent?) {
                (holder as Focusable).hasFocus = true
            }

            override fun mouseExited(e: MouseEvent?) {
                (holder as Focusable).hasFocus = false
            }
        })
    }
}
