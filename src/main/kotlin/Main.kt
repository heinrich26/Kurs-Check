import data.*
import gui.*
import gui.Consts.HOME_POLY
import gui.Consts.SIDEBAR_SIZE
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.reflect.KClass

class Main : JPanel() {
    private var wahlData: KurswahlData = KurswahlData()
    private val fachData: FachData = testFachdata

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Windows UI verwenden
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
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
            frame.minimumSize = Dimension(640, 560)
            // Display the window.
            frame.pack()
            frame.isVisible = true
        }
    }

    private val header: JPanel = JPanel(BorderLayout())

    private var curPanel: KurswahlPanel = Overview(wahlData, fachData)

    // Nav Bar Logik
    private val sidebar = JPanel(GridBagLayout()).apply {
        this.preferredSize = Dimension(SIDEBAR_SIZE, -1)
    }

    private val sidebarBtns = arrayOf(
        FsWpfIcon { navTo(Fremdsprachen::class, 0) },
        SidebarLabel("LKs") { navTo(Leistungskurse::class, 1) },
        SidebarLabel("PKs") { navTo(Fremdsprachen::class, 2) },
        SidebarLabel("GKs") { navTo(GrundkursWahl::class, 3) },
        PolyIcon(HOME_POLY, true) { navTo(Overview::class, 4) }
    ).apply {
        this.forEachIndexed { i, dest ->
            dest.let {
                sidebar.add(it, row = i, anchor = GridBagConstraints.SOUTH, weighty = if (i == 4) 1.0 else 0.0)
            }
        }
    }

    private fun <T : KurswahlPanel> navTo(panel: KClass<T>, selectedIndex: Int) {
        if (!curPanel.isDataValid()) {
            val choice = JOptionPane.showConfirmDialog(
                this,
                "Deine Daten sind ungültig und gehen verloren, wenn du jetzt weitergehst!",
                "Ungültige Daten",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            )
            if (choice == JOptionPane.CANCEL_OPTION) return // Abbruch durch Nutzer
        } else wahlData = curPanel.close()

        remove(curPanel)

        curPanel = panel.constructors.first().call(wahlData, fachData)
        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        validate()


        // Sidebar Knöpfe updaten
        for ((i, dest) in sidebarBtns.withIndex()) dest.isEnabled = i == selectedIndex
    }

    private val titleLabel = JLabel("Kurswahl App", SwingConstants.LEFT).apply {
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

        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        add(sidebar, row = 1, column = 0, fill = GridBagConstraints.VERTICAL, weighty = 1.0)
    }
}
