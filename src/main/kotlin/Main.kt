// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import data.Fach
import data.KurswahlData
import gui.BottomShadowBorder
import gui.FsWpfIcon
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class Main : JPanel() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Windows UI verwenden
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            SwingUtilities.invokeLater { createAndShowGUI() }
        }

        private fun createAndShowGUI() {
            val frame = JFrame("kurswahlApp")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            // Set up the content pane.
            frame.contentPane = Main()
            frame.minimumSize = Dimension(640, 480)
            // Display the window.
            frame.pack()
            frame.isVisible = true
        }
    }

    val header: JPanel = JPanel(BorderLayout())

    val sidebar = JPanel(GridBagLayout()).apply {
        this.preferredSize = Dimension(72, -1)
    }

    val sidebarBtns = arrayOf(FsWpfIcon(), JLabel("LKs"), JLabel("PKs"), JLabel("GKs")).let { arr ->
        arr.forEachIndexed { i, it ->
            it.isOpaque = true
            it.background = Color.RED
            it.preferredSize = Dimension(72, 72)
            it.minimumSize = Dimension(72, 72)
            sidebar.add(it, row = i)
        }
    }

    val titleLabel = JLabel("Kurswahl App", SwingConstants.LEFT).apply {
        this.font = font.deriveFont(Font.BOLD, 20f)
        this.foreground = Color.WHITE
        this.border = EmptyBorder(0, 8, 0, 0)
        header.add(this)
    }

    init {
        layout = GridBagLayout()

        header.background = Color(96, 2, 238)
        header.border = BottomShadowBorder(8)
        header.preferredSize = Dimension(-1, 64)
        header.isOpaque = false

        add(header, fill = GridBagConstraints.HORIZONTAL, row = 0, column = 0, columnspan = 3)


        add(
            JSeparator(JSeparator.VERTICAL), row = 0, column = 1, rowspan = 2,
            fill = GridBagConstraints.VERTICAL, weighty = 1.0
        )

        add(JPanel().apply {
            background = Color.RED
        }, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        add(sidebar, row = 1, column = 0, fill = GridBagConstraints.VERTICAL, weighty = 1.0)
    }
}
