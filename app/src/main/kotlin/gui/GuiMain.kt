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

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kurswahlApp.KurswahlFileFilter
import com.kurswahlApp.PngFileFilter
import com.kurswahlApp.data.Consts.APP_ICONS
import com.kurswahlApp.data.Consts.APP_NAME
import com.kurswahlApp.data.Consts.FILETYPE_EXTENSION
import com.kurswahlApp.data.Consts.HOME_POLY
import com.kurswahlApp.data.Consts.IMPORT_ICON
import com.kurswahlApp.data.Consts.PERSON_ICON
import com.kurswahlApp.data.Consts.SAVE_ICON
import com.kurswahlApp.data.Consts.SIDEBAR_SIZE
import com.kurswahlApp.data.Consts.TEST_FILE_NAME
import com.kurswahlApp.data.FachData
import com.kurswahlApp.data.KurswahlData
import com.kurswahlApp.getResourceURL
import com.kurswahlApp.readDataStruct
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.reflect.KClass


class GuiMain(file: File? = null) : JPanel() {
    private val fachData: FachData = readDataStruct()
    private var wahlData: KurswahlData =
        file?.let { loadKurswahlFile(it)?.apply { updatePflichtfaecher() } }
            ?: KurswahlData(gks = fachData.pflichtfaecher, pflichtfaecher = fachData.pflichtfaecher)


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser(APP_NAME)

            val input by parser.argument(
                ArgType.String, "input",
                "Die zum ??ffnen verwendete Datei"
            ).optional()

            val useTestData by parser.option(ArgType.Boolean, "useTestData", description = "Testdaten verwenden")
                .default(false)

            parser.parse(args)

            run(input, useTestData)
        }

        private fun run(file: String?, useTestData: Boolean) {
            // System UI verwenden
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

                // Font Hack
                /*for ((key, value) in UIManager.getLookAndFeelDefaults()) {
                    if (key is String && key.endsWith(".font")) {
                        // Hack f??r WindowsLookAndFeel
                        if (value is UIDefaults.ActiveValue) {
                            val val2 = value.createValue(UIManager.getDefaults())
                            if (val2 is FontUIResource)
                                UIManager.put(key, FontUIResource(FONT_NAME, val2.style, val2.size))
                        } else if (value is FontUIResource) // Hack f??r den Standard LookAndFeel
                            UIManager.put(key, FontUIResource(FONT_NAME, value.style, value.size))
                    }
                }*/
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            SwingUtilities.invokeLater { createAndShowGUI(file, useTestData) }
        }

        private fun createAndShowGUI(file: String?, useTestData: Boolean) {
            val frame = JFrame(APP_NAME)
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.iconImages = APP_ICONS.map { ImageIO.read(getResourceURL(it)) }

            // contentPane setzen
            // wenn vorhanden, Testdatei/gesetzte Datei laden
            frame.contentPane =
                if (useTestData) GuiMain(File(getResourceURL(TEST_FILE_NAME)!!.toURI()))
                else if (file != null) GuiMain(File(file))
                else GuiMain()

            frame.minimumSize = Dimension(640, 560)
            // Anzeigen.
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
        PolyIcon(PERSON_ICON, false) { navTo(Nutzerdaten::class, 0) },
        FsWpfIcon { navTo(Fremdsprachen::class, 1) },
        SidebarLabel("LKs") { navTo(Leistungskurse::class, 2) },
        SidebarLabel("PKs") { navTo(Pruefungsfaecher::class, 3) },
        SidebarLabel("GKs") { navTo(GrundkursWahl::class, 4) },
        PolyIcon(HOME_POLY, true) { navTo(Overview::class, 5) }
    ).apply {
        this.forEachIndexed { i, dest ->
            dest.let {
                sidebar.add(it, row = i, anchor = GridBagConstraints.SOUTH, weighty = if (i == 5) 1.0 else 0.0)
            }
        }
    }

    private fun navTo(panel: KClass<out KurswahlPanel>, index: Int) {
        if (!curPanel.isDataValid()) {
            val choice = JOptionPane.showConfirmDialog(
                this,
                "Deine ??nderungen sind ung??ltig und gehen verloren, wenn du jetzt weitergehst!",
                "Ung??ltige Daten",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            )
            if (choice == JOptionPane.CANCEL_OPTION) return // Abbruch durch Nutzer
        } else wahlData = curPanel.close()

        disableDestinations()

        swapPanel(panel, index)

        // Sidebar Kn??pfe updaten
        for ((i, dest) in sidebarBtns.withIndex()) dest.isSelected = i == index
    }

    /**
     * Tauscht das Aktuelle Panel durch das gegebene [panel] aus!
     */
    private fun swapPanel(panel: KClass<out KurswahlPanel> = curPanel::class, index: Int = -1) {
        remove(curPanel)

        val callback: (Boolean) -> Unit =
            when (index) {
                -1 -> curPanel.notifier
                in 1..3 -> { it ->
                    if (it)
                        sidebarBtns[index + 1].isEnabled = true
                    else
                        for (i in (index + 1)..4)
                            sidebarBtns[i].isEnabled = false
                }
                else -> { _ -> }
            }

        curPanel = panel.constructors.first().call(wahlData, fachData, callback)
        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        toolbar.text = curPanel.windowName

        validate()
    }

    init {
        layout = GridBagLayout()


        add(toolbar, fill = GridBagConstraints.HORIZONTAL, row = 0, column = 0, columnspan = 3)

        toolbar.addActionItem(IMPORT_ICON, "open-action") {
            val chooser = JFileChooser()
            chooser.fileFilter = KurswahlFileFilter
            chooser.dialogTitle = "Kurswahl-Datei ??ffnen"

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
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
                            "Die Version deiner Datei ist neuer als die, des Programms! Unter umst??nden gehen ein paar Daten verloren!",
                            "Versionsunterschiede",
                            JOptionPane.WARNING_MESSAGE
                        )
                    data.readJsonVersion.second < FachData.jsonVersion.second ->
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist ??lter als die Programmversion! Unter umst??nden m??ssen ein paar Daten neu eingetragen werden!",
                            "Versionsunterschiede",
                            JOptionPane.WARNING_MESSAGE
                        )
                }
                wahlData = data

                // Das GUI updaten
                swapPanel()

                disableDestinations()
            }
        }

        toolbar.addActionItem(SAVE_ICON, "save-action") {
            if (!curPanel.isDataValid()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Dein Wahl ist noch unvollst??ndig oder ung??ltig! Vervollst??ndige deine Wahl, bevor du sie exportieren kannst",
                    "Ung??ltige Wahl",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionItem
            }

            val data = curPanel.close()
            data.lock()

            val todos = data.check()

            if (todos != null) {
                JOptionPane.showMessageDialog(this, todos, "Ung??ltige Wahl", JOptionPane.ERROR_MESSAGE)
            } else if (data.countCourses().sum() !in fachData.minKurse..fachData.maxKurse) {
                JOptionPane.showMessageDialog(
                    this,
                    "Du hast noch nicht gen??gend Grundkurse! Es tut uns leid, aber du musst deine Wahl vervollst??ndigen, bevor du sie exportieren kannst",
                    "Ung??ltige Wahl",
                    JOptionPane.ERROR_MESSAGE
                )
            } else if (fachData.regeln.all { it.match(data) }) {
                val chooser = JFileChooser()
                chooser.fileFilter = KurswahlFileFilter
                chooser.dialogTitle = "Datei f??r den Oberstufenkoordinator speichern"
                chooser.selectedFile =
                    File(
                        "${wahlData.vorname!!.split(' ')[0]}_${wahlData.nachname}"
                            .replace(Regex("[\\\\/:*?\"<>|.&$]"), "")
                            .replace(' ', '_') + ".$FILETYPE_EXTENSION"
                    )

                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val f =
                        chooser.selectedFile.let { File(it.parent, "${it.nameWithoutExtension}.$FILETYPE_EXTENSION") }

                    try {
                        f.createNewFile()

                        jacksonObjectMapper().writer()
                            .withAttribute("jsonVersion", FachData.jsonVersion.let { "${it.first}.${it.second}" })
                            .writeValue(f, data)
                    } catch (exception: SecurityException) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Du hast keine Berechtigung diese Datei zu schreiben! Versuche einen anderen Namen oder Ordner!",
                            "Keine Rechte", JOptionPane.ERROR_MESSAGE
                        )
                    }
                }

                // speichern des Bilds der Kurswahl
                if (chooser.selectedFile != null)
                    chooser.selectedFile =
                        File(chooser.currentDirectory, chooser.selectedFile.nameWithoutExtension + ".png")
                chooser.resetChoosableFileFilters()
                chooser.fileFilter = PngFileFilter
                chooser.dialogTitle = "Bild f??r dich speichern"


                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val f =
                        File("${chooser.selectedFile.parent}${File.separatorChar}${chooser.selectedFile.nameWithoutExtension}.png")
                    try {
                        val img = ScreenImage.createImage(AusgabeLayout(fachData, data))
                        ImageIO.write(img, "png", f)
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
                "Deine Wahl erf??llt nicht alle Regeln... Es tut uns leid, aber du musst deine Wahl vervollst??ndigen, bevor du sie exportieren kannst!",
                "Ung??ltige Wahl", JOptionPane.ERROR_MESSAGE
            )

            data.unlock()
        }

        (layout as GridBagLayout).rowWeights = doubleArrayOf(0.0, 1.0, .0)

        disableDestinations()

        add(
            JSeparator(JSeparator.VERTICAL), row = 0, column = 1, rowspan = 3,
            fill = GridBagConstraints.VERTICAL
        )

        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        add(sidebar, row = 1, column = 0, fill = GridBagConstraints.VERTICAL, rowspan = 2)

        val resetButton = JButton("Zur??cksetzen")
        resetButton.isFocusable = false
        resetButton.addActionListener {
            if (JOptionPane.showConfirmDialog(
                    this,
                    "M??chtest du deine komplette Wahl zur??cksetzen? Alle ungespeicherten Eingaben gehen dadurch verloren!",
                    "Zur??cksetzen?",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
            ) {
                wahlData = KurswahlData(gks = fachData.pflichtfaecher, pflichtfaecher = fachData.pflichtfaecher)
                swapPanel()
            }
        }

        add(resetButton, row = 2, column = 2, anchor = GridBagConstraints.EAST, margin = Insets(4, 4, 4, 4))
    }

    /**
     * L??d eine Kurswahl Datei
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

    /**
     * Deaktiviert die Sidebar Icons von Sektionen, die nicht anklickbar sein sollen,
     * da sie daten vorraussetzen die ungesetzt sind
     */
    private fun disableDestinations() {
        if (wahlData.fremdsprachen.isEmpty())
            for (i in 2..4) sidebarBtns[i].isEnabled = false
        else if (wahlData.lk1 == null || wahlData.lk2 == null) {
            sidebarBtns[3].isEnabled = false
            sidebarBtns[4].isEnabled = false
        } else if (wahlData.pf3 == null || wahlData.pf4 == null || wahlData.pf5 == null)
            sidebarBtns[4].isEnabled = false
        else for (i in 2..4) sidebarBtns[i].isEnabled = true
    }
}

