package gui

import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent

class FsWpfIcon(
    private val defaultColor: Color = Color(94, 99, 103),
    private val activeColor: Color = Color(96, 2, 238)
) : JComponent() {

    init {
        Dimension(72, 72).let {
            size = it
            preferredSize = it
            maximumSize = it
            minimumSize = it
        }
        addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                TODO("Not yet implemented")
            }

            override fun mousePressed(e: MouseEvent?) {
                TODO("Not yet implemented")
            }

            override fun mouseReleased(e: MouseEvent?) {
                TODO("Not yet implemented")
            }

            override fun mouseEntered(e: MouseEvent?) {
                TODO("Not yet implemented")
            }

            override fun mouseExited(e: MouseEvent?) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun paintComponent(g: Graphics) {

        g.color = defaultColor

        g.font = Font("Roboto", Font.BOLD, 20)
        val metrics = g.fontMetrics
        val fsLen = metrics.stringWidth("FS")
        val wpfLen = metrics.stringWidth("WPF")

        g.drawRoundRect(0, 0, width - 1, height - 1, 12, 12)
        g.drawString("FS", 8, 36)
        g.drawString("WPF", 18, 56)
        g.font = g.font.deriveFont(16f)
        g.drawString("s", 8 + fsLen, 36)
        g.drawString("s", 18 + wpfLen, 56)

        super.paintComponent(g)
    }
}