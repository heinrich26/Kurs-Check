/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
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

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kurswahlApp.R
import com.kurswahlApp.data.*
import com.kurswahlApp.data.Consts.APP_ICONS
import com.kurswahlApp.data.Consts.APP_NAME
import com.kurswahlApp.data.Consts.FILETYPE_EXTENSION
import com.kurswahlApp.data.Consts.PANEL_WIDTH
import com.kurswahlApp.data.Consts.SIDEBAR_SIZE
import com.kurswahlApp.data.Consts.TEST_FILE_NAME
import com.kurswahlApp.github_status.GithubStatus
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.system.exitProcess


class GuiMain(file: File? = null) : JPanel() {
    private lateinit var fachData: FachData
    private lateinit var wahlData: KurswahlData
    private var currentSchool: School? = null

    private var lastFilename: File? = null


    init {
        SchoolConfig.updateConfig()

        if (file == null) {
            val lastSchool = SchoolConfig.loadLastSchool()
            if (lastSchool != null) {
                val data = try {
                    SchoolConfig.getSchool(lastSchool)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                if (data != null) {
                    updateFachData(data)
                    wahlData = data.createKurswahl()
                } else {
                    showLoadingError()
                    showSchoolChooser()
                }
            } else showSchoolChooser()
        } else if (!loadKurswahlFile(file)) {
            showLoadingError()
            showSchoolChooser()
        }

    }

    /**
     * Fordert den Nutzer auf eine Schule zu wählen
     */
    private fun showSchoolChooser(@Suppress("SameParameterValue") initial: Boolean = true) {
        chooseSchool()?.let {
            // die Auswahl hat sich nicht geändert
            if (it == currentSchool) return

            val data = try {
                SchoolConfig.getSchool(it.schulId)
            } catch (e: Exception) {
                e.printStackTrace()
                // Die Konfiguration war inkompatibel mit der Version von Kurs-Check/ist ungültig.
                null
            }

            if (data != null) {
                updateFachData(data)
                wahlData = data.createKurswahl()

                if (!initial) reloadToStart()
            } else {
                showLoadingError()
                if (initial) exitProcess(0)
            }

        } // der Nutzer hat die Auswahl abgebrochen
    }

    companion object {
        val MappedIcons = APP_ICONS.map { ImageIO.read(getResourceURL(it)) }
        private const val UNVOLLSTAENDIGE_EINGABE_TEXT = "Eingabe unvollständig..."

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser(APP_NAME)

            val input by parser.argument(
                ArgType.String, "input", "Die zum Öffnen verwendete Datei"
            ).optional()


            val useTestData by parser.option(ArgType.Boolean, "useTestData", description = "Testdaten verwenden")
                .default(false)

            parser.parse(args)


            // System UI verwenden
            try {
                prepareUI()
            } catch (_: Exception) {}

            SwingUtilities.invokeLater { createAndShowGUI(input, useTestData) }
        }

        @JvmStatic
        private fun createAndShowGUI(file: String?, useTestData: Boolean) {
            val frame = JFrame(APP_NAME)
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.iconImages = MappedIcons

            // contentPane setzen
            // wenn vorhanden, Testdatei/gesetzte Datei laden
            frame.contentPane = if (useTestData) GuiMain(File(getResourceURL(TEST_FILE_NAME)!!.toURI()))
            else if (file != null) GuiMain(File(file))
            else GuiMain()

            frame.minimumSize = 720 by 640
            // Anzeigen.
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }

    private var curPanel: KurswahlPanel = Overview(wahlData, fachData)

    private val toolbar = Toolbar(curPanel.windowName)

    private val unvollstaendigeEingabeLabel: JLabel = JLabel(UNVOLLSTAENDIGE_EINGABE_TEXT).also {
        it.isVisible = false
        it.foreground = Consts.COLOR_ERROR
        it.font = font.deriveFont(12f)
    }

    private val chooseSchoolButton = JButton(currentSchool!!.shortname)

    // Nav Bar Logik
    private val sidebar = JPanel(GridBagLayout()).apply {
        this.preferredSize = SIDEBAR_SIZE by 0
    }

    private val sidebarBtns = arrayOf(
        PolyIcon(R.person, false) { navTo(Nutzerdaten::class, 0) },
        FsWpfIcon { navTo(Fremdsprachen::class, 1) },
        SidebarLabel("LKs") { navTo(Leistungskurse::class, 2) },
        PfPkIcon { navTo(Pruefungsfaecher::class, 3) },
        SidebarLabel("GKs") { navTo(GrundkursWahl::class, 4) },
        PolyIcon(R.priority_list, false) { navTo(UmfragePanel::class, 5) },
        PolyIcon(R.home, true) { navTo(Overview::class, 6) }).apply {
        this.forEachIndexed { i, dest ->
            sidebar.add(dest, row = i, anchor = GridBagConstraints.SOUTH, weighty = if (i == 6) 1.0 else 0.0)
        }
    }

    private fun navTo(panel: KClass<out KurswahlPanel>, index: Int) {
        if (!curPanel.isDataValid()) {
            val choice = JOptionPane.showConfirmDialog(
                this,
                R.getString("data_loss_alert"),
                R.getString("invalid_data"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            )
            if (choice == JOptionPane.CANCEL_OPTION) return // Abbruch durch Nutzer
        } else wahlData = curPanel.close()

        disableDestinations()

        swapPanel(panel, index)

        // Sidebar Knöpfe updaten
        for ((i, dest) in sidebarBtns.withIndex()) dest.isSelected = i == index
    }

    /**
     * Geht zum Startbildschirm zurück und händelt die Änderungen an der Toolbar
     */
    private fun reloadToStart() {
        disableDestinations()

        swapPanel(Overview::class, 6)

        // Sidebar Knöpfe updaten
        for ((i, dest) in sidebarBtns.withIndex()) dest.isSelected = i == 6
    }

    /**
     * Tauscht das aktuelle Panel durch das gegebene [panel] aus!
     */
    private fun swapPanel(panel: KClass<out KurswahlPanel> = curPanel::class, index: Int = -1) {
        remove(curPanel)

        val callback: (Boolean) -> Unit = when (index) {
            -1 -> curPanel.notifier
            in 1..3 -> { it ->
                // TODO Fixen, dass man wenn man eine komplette Wahl hat,
                //  von den LKS nicht direkt zu den Grundkursen gehen kann! (Non Breaking)
                unvollstaendigeEingabeLabel.isVisible = !it
                if (it) sidebarBtns[index + 1].isEnabled = true
                else for (i in (index + 1)..4) sidebarBtns[i].isEnabled = false
            }

            0, 4, 5 -> { it -> unvollstaendigeEingabeLabel.isVisible = !it }
            else -> { _ -> }
        }

        curPanel = panel.constructors.first().call(wahlData, fachData, callback)
        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)

        toolbar.text = curPanel.windowName

        unvollstaendigeEingabeLabel.isVisible = !curPanel.isDataValid()

        chooseSchoolButton.text = currentSchool!!.shortname

        validate()
    }

    init {
        layout = GridBagLayout()

        add(toolbar, fill = GridBagConstraints.HORIZONTAL, row = 0, column = 0, columnspan = 3)
        toolbar.addActionItem(R.file_open, "open-action", R.getString("open_kurswahl"), this::openKurswahlAction)
        toolbar.addActionItem(R.save, "save-action", R.getString("save_kurswahl"), this::saveKurswahlAction)
        toolbar.addActionItem(R.reset, "reset-action", R.getString("reset"), this::resetWahl)
        toolbar.addActionItem(R.help, "help-action", R.getString("help"), this::showHelp)

        (layout as GridBagLayout).rowWeights = doubleArrayOf(0.0, 1.0, .0)

        disableDestinations()

        add(
            JSeparator(JSeparator.VERTICAL), row = 0, column = 1, rowspan = 3, fill = GridBagConstraints.VERTICAL
        )

        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)
        add(sidebar, row = 1, column = 0, fill = GridBagConstraints.VERTICAL, rowspan = 2)

        add(unvollstaendigeEingabeLabel, row = 2, column = 2, anchor = GridBagConstraints.CENTER)


        chooseSchoolButton.addMouseListener(onEnter = { chooseSchoolButton.text = "\u2190 Schule wechseln" },
            onExit = { chooseSchoolButton.text = currentSchool!!.shortname })

        chooseSchoolButton.isFocusable = false
        chooseSchoolButton.foreground = Consts.COLOR_PRIMARY
        chooseSchoolButton.addActionListener {
            if (JOptionPane.showConfirmDialog(
                    this,
                    R.getString("change_school"),
                    "${R.getString("continue")}?",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
            ) showSchoolChooser(false)
        }

        add(chooseSchoolButton, row = 2, column = 2, anchor = GridBagConstraints.WEST, margin = Insets(4))

        // Umfragen Knopf abhängig von der Schul-Konfig verstecken
        sidebarBtns[5].isVisible = fachData.umfragen.isNotEmpty()
    }

    /**
     * Zeigt den Hilfedialog jedes Panels an
     */
    @Suppress("CssUnknownProperty", "CssInvalidPropertyValue")
    private fun showHelp() {
        val content = JEditorPane("text/html", /*language=html*/ "<html><body><h1><a name='top'>Kurs-Check Hife</a></h1>${curPanel.showHelp()}</body></html>")
        val pane = JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        content.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        content.isEditable = false
        content.isOpaque = false
        content.font = JLabel().font
        content.maximumSize = Dimension(PANEL_WIDTH - pane.verticalScrollBar.preferredSize.width - 2, Int.MAX_VALUE)
        with((content.editorKit as HTMLEditorKit).styleSheet) {
            addRule(/*language=css*/ "a, h1, h2, h3, h4, h5, h6 {color: ${Consts.COLOR_PRIMARY.hexString()}}")
            addRule(/*language=css*/ "p { margin-top: 15; margin-bottom: 15 }")
            addRule(/*language=css*/ "ol, ul { margin-left-ltr: 25; margin-right-rtl: 25 }")
        }
        content.addHyperlinkListener {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED && it.description != null) {
                if (it.description.startsWith('#')) {
                    content.scrollToReference(it.description.substring(1))
                } else {
                    openWebpage(it.url)
                }
            }
        }

        pane.preferredSize = 500 by 400

        // Wartet dass der Dialog angezeigt wird und scrollt dann nach oben.
        pane.addAncestorListener(object : AncestorListener {
            override fun ancestorAdded(event: AncestorEvent) {
                content.scrollToReference("top")
                pane.removeAncestorListener(this)
            }

            override fun ancestorRemoved(event: AncestorEvent) {}
            override fun ancestorMoved(event: AncestorEvent) {}
        })

        JOptionPane.showMessageDialog(this, pane, R.getString("help"), JOptionPane.PLAIN_MESSAGE)
    }

    /**
     * Aktualisiert die FachData und alle damit verbundenen Daten
     */
    private fun updateFachData(data: FachData) {
        this.fachData = data
        this.currentSchool = SchoolConfig.schools.find { it.schulId == data.schulId }
        thread { SchoolConfig.writeLastSchool(data.schulId) }

        try {
            sidebarBtns[5].isVisible = data.umfragen.isNotEmpty()
        } catch (_: NullPointerException) {} // sind grade am Initialisieren, wird in der init {} erledigt
    }

    /**
     * Läd eine Kurswahl Datei
     */
    private fun loadKurswahlFile(file: File): Boolean {
        if (file.extension != FILETYPE_EXTENSION) return false

        if (file.exists() && file.canRead()) {
            val mirror = try {
                FachDataMirror(if (this::fachData.isInitialized) fachData else null, SchoolConfig::getSchool)
            } catch (_: Exception) {
                return false
            }

            val mapper = jacksonObjectMapper()
            mapper.injectableValues = InjectableValues.Std().addValue(FachDataMirror::class.java, mirror)

            try {
                val data = mapper.readValue<KurswahlData>(file)
                val newFachData: FachData = mirror.fachData!!
                when {
                    data.readJsonVersion.major != newFachData.jsonVersion.major -> {
                        JOptionPane.showMessageDialog(
                            this,
                            R.getString("incompatible_file_alert"),
                            R.getString("incompatible_file"),
                            JOptionPane.ERROR_MESSAGE
                        )
                        return false
                    }

                    data.readJsonVersion.minor > newFachData.jsonVersion.minor -> JOptionPane.showMessageDialog(
                        this,
                        R.getString("newer_file_alert"),
                        R.getString("version_differences"),
                        JOptionPane.WARNING_MESSAGE
                    )

                    data.readJsonVersion.major < newFachData.jsonVersion.minor -> JOptionPane.showMessageDialog(
                        this,
                        R.getString("older_file_alert"),
                        R.getString("version_differences"),
                        JOptionPane.WARNING_MESSAGE
                    )
                }
                updateFachData(newFachData)
                wahlData = data
                lastFilename = file
                data.updatePflichtfaecher()
                return true
            } catch (_: DatabindException) {
                JOptionPane.showMessageDialog(
                    this,
                    R.getString("cannot_read_alert"),
                    R.getString("bad_file"),
                    JOptionPane.ERROR_MESSAGE
                )
            } catch (_: MismatchedInputException) {
                JOptionPane.showMessageDialog(
                    this,
                    R.getString("cannot_read_alert"),
                    R.getString("bad_file"),
                    JOptionPane.ERROR_MESSAGE
                )
            } catch (_: RuntimeException) {
                showLoadingError()
            }
        }

        return false
    }

    private fun openKurswahlAction() {
        val chooser = JFileChooser()
        chooser.fileFilter = KurswahlFileFilter
        chooser.dialogTitle = R.getString("open_kurswahl_file")

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (loadKurswahlFile(chooser.selectedFile))
            // Das GUI updaten
                reloadToStart()
        }
    }

    private fun saveKurswahlAction() {
        if (!curPanel.isDataValid()) {
            JOptionPane.showMessageDialog(
                this,
                R.getString("wahl_incomplete_alert"),
                R.getString("invalid_wahl"),
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        val data = curPanel.close()
        data.lock()

        val todos = data.check(fachData)

        if (todos != null) {
            JOptionPane.showMessageDialog(this, todos, R.getString("invalid_wahl"), JOptionPane.ERROR_MESSAGE)
        } else if (data.countCourses().sum() !in fachData.minKurse..fachData.maxKurse) {
            JOptionPane.showMessageDialog(
                this,
                "${R.getString("not_enough_grundkurse")} ${R.getString("comlete_wahl_to_export")}",
                R.getString("invalid_wahl"),
                JOptionPane.ERROR_MESSAGE
            )
        } else if (!fachData.regeln.all { it.match(data) }) {
            JOptionPane.showMessageDialog(
                this,
                "${R.getString("rules_unfullfilled")} ${R.getString("comlete_wahl_to_export")}",
                R.getString("invalid_wahl"),
                JOptionPane.ERROR_MESSAGE
            )
        } else {
            val chooser = JFileChooser()

            /**
             * Speichert eine Json Variante der Kurswahl
             */
            fun saveKurswahl() {
                chooser.fileFilter = KurswahlFileFilter
                chooser.dialogTitle = R.getString("save_file_for_paeko")
                chooser.selectedFile = File(lastFilename?.parentFile, "${data.toFilename()}.$FILETYPE_EXTENSION")

                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val f = chooser.selectedFile.withExtension(FILETYPE_EXTENSION).also { lastFilename = it }

                    thread {
                        try {
                            f.createNewFile()

                            jacksonObjectMapper().writer()
                                .withAttribute("jsonVersion", fachData.jsonVersion)
                                .writeValue(f, data)
                        } catch (_: SecurityException) {
                            JOptionPane.showMessageDialog(
                                this,
                                R.getString("missing_write_permission"),
                                R.getString("no_rights"),
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            }

            /**
             * Speichert ein Bild der Kurswahl
             */
            fun saveImage() {
                if (chooser.selectedFile != null) chooser.selectedFile = chooser.selectedFile.withExtension("pdf")
                chooser.fileFilter = PdfFileFilter
                chooser.dialogTitle = R.getString("save_for_you")


                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val f = chooser.selectedFile.withExtension("pdf")

                    thread {
                        try {
                            val img = ScreenImage.createImage(AusgabeLayout(fachData, data), scale = 3.0)
                            createAndSaveImagePDF(f, img)
                        } catch (_: SecurityException) {
                            JOptionPane.showMessageDialog(
                                this,
                                R.getString("missing_write_permission"),
                                R.getString("no_rights"),
                                JOptionPane.ERROR_MESSAGE
                            )
                        } catch (_: FileNotFoundException) {
                            // zugriff auf PDF nicht mögich, da in anderer App geöffnet
                            JOptionPane.showMessageDialog(
                                this,
                                R.getString("file_locked"),
                                R.getString("fileaccess_not_possible"),
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            }

            fun askFallback() {
                if (JOptionPane.showConfirmDialog(
                        this,
                        R.getString("no_form_selected_alert"),
                        R.getString("nothing_selected"),
                        JOptionPane.YES_NO_OPTION
                    ) == JOptionPane.YES_OPTION
                ) {
                    saveKurswahl()
                }
            }

            if (fachData.nutztLusd) {
                chooser.fileFilter = LusdPdfFileFilter
                chooser.dialogTitle = R.getString("open_lusd_file")

                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    val formFile = chooser.selectedFile

                    chooser.fileFilter = ExclusivePdfFileFilter(formFile)
                    chooser.dialogTitle = R.getString("save_filled_form")
                    chooser.selectedFile = File(formFile.parent, data.toFilename() + ".pdf")

                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        val dest = chooser.selectedFile.withExtension("pdf")
                        thread {
                            try {
                                data.exportPDF(formFile, dest, fachData)
                            } catch (_: IOException) {
                                JOptionPane.showMessageDialog(
                                    this,
                                    R.getString("saving_error_alert"),
                                    R.getString("saving_error"),
                                    JOptionPane.ERROR_MESSAGE
                                )
                            }
                        }
                    } else askFallback()
                } else askFallback()
            } else {
                saveKurswahl()
                saveImage()
            }
        }
    }

    /**
     * Setzt die Wahl zurück
     */
    private fun resetWahl() {
        if (JOptionPane.showConfirmDialog(
                this,
                R.getString("ask_reset"),
                "${R.getString("reset")}?",
                JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION
        ) {
            wahlData = fachData.createKurswahl()
            reloadToStart()
        }
    }

    /**
     * Benachrichtigt den Nutzer über einen Fehler beim Laden der Schul-Daten von GitHub
     */
    private fun showLoadingError() {
        val githubStatus = GithubStatus.get()
        val msg = if (githubStatus == null) R.getString("ensure_internet_alert")
        else if (githubStatus.indicator == GithubStatus.Companion.Status.NONE) R.getString("loading_error_alert")
        else "${R.getString("github_server_unreachable")} <a href=''>githubstatus.com</a>"

        val pane = JEditorPane("text/html", "${R.getString("school_loading_error_alert")} $msg".wrapHtml())
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        pane.isEditable = false
        pane.isOpaque = false
        pane.font = JLabel().font
        pane.addHyperlinkListener {
            if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                openWebpage(URL("https://www.githubstatus.com"))
            }
        }
        JOptionPane.showMessageDialog(
            this,
            pane,
            R.getString("school_loading_error"),
            JOptionPane.ERROR_MESSAGE
        )
    }


    /**
     * Dialog um eine Schule auszuwählen
     * @return neuausgewählte Schule oder `null`
     */
    private fun chooseSchool(): School? {

        val listModel = DefaultListModel<School>()
        listModel.addAll(SchoolConfig.schools)
        val schoolList = JList(listModel)

        // Ausgangswert setzen
        schoolList.selectedIndex = if (currentSchool == null) 0
        else listModel.indexOf(currentSchool)

        schoolList.cellRenderer = SchoolRenderer()
        val pane = JScrollPane(schoolList)
        pane.preferredSize = 242 by 298

        schoolList.fixedCellHeight = -1

        val l: ComponentListener = object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                // for core: force cache invalidation by temporarily setting fixed height
                schoolList.fixedCellHeight = 10
//                schoolList.fixedCellHeight = 56
                schoolList.fixedCellHeight = -1
            }
        }

        schoolList.addComponentListener(l)
        val layout = JPanel(GridBagLayout())
        layout.add(pane, row = 0, column = 0, columnspan = 2, weightx = 1.0)

        // TODO mit JOptionPane machen
        val dialog = JDialog(null as JDialog?, R.getString("choose_school"), true)
        var result = -1

        val okBtn = JButton(R.getString("ok"))
        val btnDim = 100 by 25
        okBtn.preferredSize = btnDim
        okBtn.addActionListener {
            result = JOptionPane.OK_OPTION
            dialog.dispose()
        }

        if (currentSchool != null) {
            layout.add(
                okBtn,
                row = 1,
                column = 0,
                anchor = GridBagConstraints.EAST,
                margin = Insets(top = 9, bottom = 2),
                weightx = 1.0
            )
            layout.add(
                JButton(R.getString("cancel")).apply {
                    this.preferredSize = btnDim
                    this.addActionListener {
                        result = JOptionPane.CANCEL_OPTION
                        dialog.dispose()
                    }
                },
                row = 1,
                column = 1,
                anchor = GridBagConstraints.WEST,
                margin = Insets(top = 9, bottom = 2),
                weightx = 1.0
            )
        } else layout.add(okBtn, row = 1, column = 0, columnspan = 2, margin = Insets(top = 9, bottom = 2))

        layout.border = EmptyBorder(10, 10, 10, 10)

        dialog.add(layout)
        dialog.isResizable = false
        dialog.defaultCloseOperation =
            if (currentSchool == null) WindowConstants.DO_NOTHING_ON_CLOSE else WindowConstants.DISPOSE_ON_CLOSE
        dialog.iconImages = MappedIcons
        dialog.pack()
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true

        // Dialog ausführen, warten auf Nutzereingabe...

        return when (result) {
            JOptionPane.OK_OPTION -> schoolList.selectedValue
            -1 -> if (currentSchool == null) exitProcess(0) else null
            else -> null
        }
    }

    /**
     * Deaktiviert die Sidebar Icons von Sektionen, die nicht anklickbar sein sollen,
     * da sie daten vorraussetzen die ungesetzt sind
     */
    private fun disableDestinations() {
        if (wahlData.fremdsprachen.size < 2 || wahlData.wpfs == null) for (i in 2..4) sidebarBtns[i].isEnabled = false
        else if (wahlData.lk1 == null || wahlData.lk2 == null) {
            sidebarBtns[3].isEnabled = false
            sidebarBtns[4].isEnabled = false
        } else if (wahlData.pf3 == null || wahlData.pf4 == null || wahlData.pf5 == null) sidebarBtns[4].isEnabled =
            false
        else for (i in 2..4) sidebarBtns[i].isEnabled = true
    }
}