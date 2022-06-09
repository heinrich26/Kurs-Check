
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import data.FachData
import data.KurswahlData
import gui.*
import gui.Consts.FILETYPE_EXTENSION
import gui.Consts.HOME_POLY
import gui.Consts.IMPORT_ICON
import gui.Consts.SAVE_ICON
import gui.Consts.SIDEBAR_SIZE
import gui.Consts.TEST_FILE_NAME
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.reflect.KClass


class Main(file: File? = null) : JPanel() {
    private val fachData: FachData = readDataStruct()
    private var wahlData: KurswahlData =
        file?.let { loadKurswahlFile(it)?.apply { this.gks += fachData.pflichtfaecher.filter { (fach, _) -> fach !in this.pfs } } }
            ?: KurswahlData(gks = fachData.pflichtfaecher)


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Windows UI verwenden
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            SwingUtilities.invokeLater { createAndShowGUI(args) }
        }

        private fun createAndShowGUI(args: Array<String>) {
            val frame = JFrame("kurswahlApp")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            // Set up the content pane
            // eventuell Testdatei/gesetzte Datei laden
            frame.contentPane =
                if ("--useTestData" in args) Main(File(getResourceURL(TEST_FILE_NAME)!!.toURI()))
                else if (args.isNotEmpty() && !args[0].startsWith("--")) Main(File(args[0]))
                else Main()

            frame.minimumSize = Dimension(640, 560)
            // Display the window.
            frame.pack()
            frame.setLocation(500, 200)
            frame.isVisible = true
        }
    }

    private var curPanel: KurswahlPanel = Overview(this.wahlData, fachData)

    private val toolbar = Toolbar(curPanel.windowName)

    // Nav Bar Logik
    private val sidebar = JPanel(GridBagLayout()).apply {
        this.preferredSize = Dimension(SIDEBAR_SIZE, 0)
    }

    private val sidebarBtns = arrayOf(
        FsWpfIcon { navTo(Fremdsprachen::class, 0) },
        SidebarLabel("LKs") { navTo(Leistungskurse::class, 1) },
        SidebarLabel("PKs") { navTo(Pruefungsfaecher::class, 2) },
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

        toolbar.text = curPanel.windowName

        validate()

        // Sidebar Knöpfe updaten
//        disableDestinations(selectedIndex + 2)
        for ((i, dest) in sidebarBtns.withIndex()) dest.isSelected = i == selectedIndex
    }

    /*private fun disableDestinations(min: Int = 1) {
        val start: Int = max(min,
            if (wahlData.fremdsprachen.isEmpty()) 1
            else when (wahlData.pfs.indexOfFirst { it == null }) {
                0, 1 -> 2
                -1 -> 4
                else -> 3
            })
        for (i in 0..3) sidebarBtns[i].isEnabled = i < start
    }*/

//    private val titleLabel = JLabel(curPanel.windowName, SwingConstants.LEFT).apply {
//        this.font = font.deriveFont(Font.BOLD, 20f)
//        this.foreground = Color.WHITE
//        toolbar.add(this, anchor = GridBagConstraints.WEST, fill = GridBagConstraints.HORIZONTAL, weightx = 1.0)
//    }

    init {
        layout = GridBagLayout()


        add(toolbar, fill = GridBagConstraints.HORIZONTAL, row = 0, column = 0, columnspan = 3)

        toolbar.addActionItem(IMPORT_ICON, "open-action") {
            val chooser = JFileChooser()
            chooser.fileFilter = KurswahlFileFilter
            val ans = chooser.showOpenDialog(this)
            if (ans == JFileChooser.APPROVE_OPTION) {
                val data: KurswahlData = loadKurswahlFile(chooser.selectedFile) ?: return@addActionItem

                when {
                    data.readJsonVersion.first != FachData.jsonVersion.first -> {
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist inkompatibel! Es tut uns leid, aber du musst deine Wahl erneut eingeben!",
                            "Inkompatible Datei",
                            JOptionPane.ERROR_MESSAGE
                        )
                        return@addActionItem
                    }
                    data.readJsonVersion.second > FachData.jsonVersion.second ->
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist neuer als die, des Programms! Unter umständen gehen ein paar Daten verloren!",
                            "Versionsunterschiede",
                            JOptionPane.WARNING_MESSAGE
                        )
                    data.readJsonVersion.second < FachData.jsonVersion.second ->
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist älter als die Programmversion! Unter umständen müssen ein paar Daten neu eingetragen werden!",
                            "Versionsunterschiede",
                            JOptionPane.WARNING_MESSAGE
                        )
                }
                this.wahlData = data

                // Das GUI updaten
                remove(curPanel)

                curPanel = curPanel::class.constructors.first().call(this.wahlData, fachData)
                add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

                validate()
            }
        }

        toolbar.addActionItem(SAVE_ICON, "save-action") {
            this.wahlData.lock()

            if (this.wahlData.isComplete &&
                this.wahlData.countCourses().sum() in fachData.minKurse..fachData.maxKurse &&
                fachData.regeln.all { it.match(this.wahlData) }
            ) {
                val chooser = JFileChooser()
                chooser.fileFilter = KurswahlFileFilter

                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val f =
                        chooser.selectedFile.let { File(it.parent, "${it.nameWithoutExtension}.$FILETYPE_EXTENSION") }

                    try {
                        f.createNewFile()

                        jacksonObjectMapper().writer()
                            .withAttribute("jsonVersion", FachData.jsonVersion.let { "${it.first}.${it.second}" })
                            .writeValue(f, this.wahlData)
                    } catch (exception: SecurityException) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Du hast keine Berechtigung diese Datei zu schreiben! Versuche einen anderen Namen oder Ordner!",
                            "Keine Rechte", JOptionPane.ERROR_MESSAGE
                        )
                    }
                }

                // TODO speichern des Bilds der Kurswahl
                chooser.resetChoosableFileFilters()
                chooser.fileFilter = PngFileFilter

                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val f =
                        File("${chooser.selectedFile.parent}${File.separatorChar}${chooser.selectedFile.nameWithoutExtension}.png")
                    try {
                        // TODO bild speichern

                        val img = ScreenImage.createImage(AusgabeLayout(fachData, this.wahlData))
                        ImageIO.write(img, "png", f)


                        // PNG ist superior
                        /*val jpegParams = JPEGImageWriteParam(null)
                        jpegParams.compressionMode = ImageWriteParam.MODE_EXPLICIT
                        jpegParams.compressionQuality = 1f

                        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
                        // Outputlocation setzen
                        writer.output = FileImageOutputStream(f)

                        // Speichert das Bild mit dem gesetzten Kompressionsniveau
                        // der JPEGImageWriteParam-Instanz
                        writer.write(null, IIOImage(img, null, null), jpegParams)*/
                    } catch (exception: SecurityException) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Du hast keine Berechtigung diese Datei zu schreiben! Versuche einen anderen Namen oder Ordner!",
                            "Keine Rechte", JOptionPane.ERROR_MESSAGE
                        )
                    }
                }


            } else JOptionPane.showMessageDialog(
                this,
                "Deine Datei ist ungültig oder unvollständig... Es tut uns leid, aber du musst deine Wahl vervollständigen bevor du sie exportieren kannst!",
                "Ungültige Wahl", JOptionPane.ERROR_MESSAGE
            )
            this.wahlData.unlock()
        }
//        disableDestinations()

        add(
            JSeparator(JSeparator.VERTICAL), row = 0, column = 1, rowspan = 2,
            fill = GridBagConstraints.VERTICAL, weighty = 1.0
        )

        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        add(sidebar, row = 1, column = 0, fill = GridBagConstraints.VERTICAL, weighty = 1.0)
    }

    /**
     * Läd eine Kurswahl Datei
     */
    private fun loadKurswahlFile(file: File): KurswahlData? {
        if (file.extension != FILETYPE_EXTENSION) return null

        if (file.exists() && file.canRead()) {
            try {
                return fachData.loadKurswahl(file)
            } catch (e: DatabindException) {
                JOptionPane.showMessageDialog(
                    this,
                    "Die Datei konnte nicht gelesen werden! Es tut uns leid, aber du musst deine Wahl erneut eingeben!",
                    "Fehlerhafte Datei",
                    JOptionPane.ERROR_MESSAGE
                )
                e.printStackTrace()
            }
        }

        return null
    }
}

