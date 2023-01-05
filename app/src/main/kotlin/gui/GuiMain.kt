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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kurswahlApp.KurswahlFileFilter
import com.kurswahlApp.PngFileFilter
import com.kurswahlApp.data.*
import com.kurswahlApp.data.Consts.APP_ICONS
import com.kurswahlApp.data.Consts.APP_NAME
import com.kurswahlApp.data.Consts.FILETYPE_EXTENSION
import com.kurswahlApp.data.Consts.HOME_POLY
import com.kurswahlApp.data.Consts.IMPORT_ICON
import com.kurswahlApp.data.Consts.PERSON_ICON
import com.kurswahlApp.data.Consts.SAVE_ICON
import com.kurswahlApp.data.Consts.SIDEBAR_SIZE
import com.kurswahlApp.data.Consts.TEST_FILE_NAME
import com.kurswahlApp.getResourceURL
import github_status.GithubStatus
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional
import java.awt.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.system.exitProcess


class GuiMain(file: File? = null) : JPanel() {
    private lateinit var fachData: FachData
    private lateinit var wahlData: KurswahlData
    private var currentSchool: School? = null

    /** Aktuallisiert die FachData und führt setzt alle damit verbundenen Daten neu */
    private fun updateFachData(data: FachData) {
        this.fachData = data
        this.currentSchool = SchoolConfig.schools.find { it.schulId == data.schulId }
        thread { SchoolConfig.writeLastSchool(data.schulId) }
    }

    init {
        SchoolConfig.updateConfig()

        if (file == null) {
            val lastSchool = SchoolConfig.loadLastSchool()
            if (lastSchool != null) {
                val data = SchoolConfig.getSchool(lastSchool)
                if (data != null) {
                    updateFachData(data)
                    wahlData = data.createKurswahl(lastSchool)
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
    private fun showSchoolChooser(initial: Boolean = true) {
        chooseSchool()?.let {
            if (it != currentSchool) {
                val data = SchoolConfig.getSchool(it.schulId)
                if (data != null) {
                    currentSchool = it
                    fachData = data
                    wahlData = data.createKurswahl(it.schulId)
                    thread { SchoolConfig.writeLastSchool(it.schulId) }

                    if (!initial) {
                        reloadToStart()
                    }
                } else {
                    showLoadingError()
                    exitProcess(0)
                }
            } // die Auswahl hat sich nicht geändert
        } // der Nutzer hat die Auswahl abgebrochen
    }

    companion object {
        val MappedIcons = APP_ICONS.map { ImageIO.read(getResourceURL(it)) }
        private const val UNVOLLSTAENDIGE_EINGABE_TEXT = "Eingabe unvollständig..."

        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser(APP_NAME)

            val input by parser.argument(
                ArgType.String, "input",
                "Die zum öffnen verwendete Datei"
            ).optional()



            val useTestData by parser.option(ArgType.Boolean, "useTestData", description = "Testdaten verwenden")
                .default(false)

            parser.parse(args)


            // System UI verwenden
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

                // Font Hack
                /*for ((key, value) in UIManager.getLookAndFeelDefaults()) {
                    if (key is String && key.endsWith(".font")) {
                        // Hack für WindowsLookAndFeel
                        if (value is UIDefaults.ActiveValue) {
                            val val2 = value.createValue(UIManager.getDefaults())
                            if (val2 is FontUIResource)
                                UIManager.put(key, FontUIResource(FONT_NAME, val2.style, val2.size))
                        } else if (value is FontUIResource) // Hack für den Standard LookAndFeel
                            UIManager.put(key, FontUIResource(FONT_NAME, value.style, value.size))
                    }
                }*/
            } catch (e: Exception) {
                e.printStackTrace()
            }

            SwingUtilities.invokeLater { createAndShowGUI(input, useTestData) }
        }

        @JvmStatic
        private fun createAndShowGUI(file: String?, useTestData: Boolean) {
            val frame = JFrame(APP_NAME)
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.iconImages = MappedIcons

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

    private var curPanel: KurswahlPanel = Overview(wahlData, fachData)

    private val toolbar = Toolbar(curPanel.windowName)

    private val unvollstaendigeEingabeLabel: JLabel = JLabel(UNVOLLSTAENDIGE_EINGABE_TEXT).also {
        it.isVisible = false
        it.foreground = Color.RED
        it.font = font.deriveFont(12f)
    }

    private val chooseSchoolButton = JButton(currentSchool!!.name)

    // Nav Bar Logik
    private val sidebar = JPanel(GridBagLayout()).apply {
        this.preferredSize = Dimension(SIDEBAR_SIZE, 0)
    }

    private val sidebarBtns = arrayOf(
        PolyIcon(PERSON_ICON, false) { navTo(Nutzerdaten::class, 0) },
        FsWpfIcon { navTo(Fremdsprachen::class, 1) },
        SidebarLabel("LKs") { navTo(Leistungskurse::class, 2) },
        PfPkIcon { navTo(Pruefungsfaecher::class, 3) },
        SidebarLabel("GKs") { navTo(GrundkursWahl::class, 4) },
        PolyIcon(HOME_POLY, true) { navTo(Overview::class, 5) }
    ).apply {
        this.forEachIndexed { i, dest ->
            sidebar.add(dest, row = i, anchor = GridBagConstraints.SOUTH, weighty = if (i == 5) 1.0 else 0.0)
        }
    }

    private fun navTo(panel: KClass<out KurswahlPanel>, index: Int) {
        if (!curPanel.isDataValid()) {
            val choice = JOptionPane.showConfirmDialog(
                this,
                "Deine Änderungen sind ungültig und gehen verloren, wenn du jetzt weitergehst!",
                "Ungültige Daten",
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

        swapPanel(Overview::class, 5)

        // Sidebar Knöpfe updaten
        for ((i, dest) in sidebarBtns.withIndex()) dest.isSelected = i == 5
    }

    /**
     * Tauscht das aktuelle Panel durch das gegebene [panel] aus!
     */
    private fun swapPanel(panel: KClass<out KurswahlPanel> = curPanel::class, index: Int = -1) {
        remove(curPanel)

        val callback: (Boolean) -> Unit =
            when (index) {
                -1 -> curPanel.notifier
                in 1..3 -> { it ->
                    // TODO Fixen, dass man wenn man eine komplette Wahl hat,
                    //  von den LKS nicht direkt zu den Grundkursen gehen kann!
                    unvollstaendigeEingabeLabel.isVisible = !it
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

        unvollstaendigeEingabeLabel.isVisible = !curPanel.isDataValid()

        chooseSchoolButton.text = currentSchool!!.name

        validate()
    }

    init {
        layout = GridBagLayout()


        add(toolbar, fill = GridBagConstraints.HORIZONTAL, row = 0, column = 0, columnspan = 3)
        toolbar.addActionItem(IMPORT_ICON, "open-action", "Kurswahl öffnen", this::openKurswahlAction)
        toolbar.addActionItem(SAVE_ICON, "save-action", "Kurswahl speichern", this::saveKurswahlAction)

        (layout as GridBagLayout).rowWeights = doubleArrayOf(0.0, 1.0, .0)

        disableDestinations()

        add(
            JSeparator(JSeparator.VERTICAL), row = 0, column = 1, rowspan = 3,
            fill = GridBagConstraints.VERTICAL
        )

        add(curPanel, row = 1, column = 2, fill = GridBagConstraints.BOTH, weightx = 1.0)
        add(sidebar, row = 1, column = 0, fill = GridBagConstraints.VERTICAL, rowspan = 2)

        add(unvollstaendigeEingabeLabel, row = 2, column = 2, anchor = GridBagConstraints.CENTER)

        val resetButton = JButton("Zurücksetzen")
        resetButton.isFocusable = false
        resetButton.addActionListener {
            if (JOptionPane.showConfirmDialog(
                    this,
                    "Möchtest du deine komplette Wahl zurücksetzen? Alle ungespeicherten Eingaben gehen dadurch verloren!",
                    "Zurücksetzen?",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
            ) {
                wahlData = fachData.createKurswahl(currentSchool!!.schulId)
                reloadToStart()
            }
        }

        add(resetButton, row = 2, column = 2, anchor = GridBagConstraints.EAST, margin = Insets(4, 4, 4, 4))


        chooseSchoolButton.addMouseListener(
            onEnter = { chooseSchoolButton.text = "\u2190 Schule wechseln" },
            onExit = { chooseSchoolButton.text = currentSchool!!.name }
        )

        chooseSchoolButton.isFocusable = false
        chooseSchoolButton.foreground = Consts.COLOR_PRIMARY
        chooseSchoolButton.addActionListener {
            if (JOptionPane.showConfirmDialog(
                    this,
                    "Möchtest du die Schule wirklich wechseln? Alle ungespeicherten Eingaben gehen dadurch verloren!",
                    "Fortfahren?",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
            ) showSchoolChooser(false)
        }

        add(chooseSchoolButton, row = 2, column = 2, anchor = GridBagConstraints.WEST, margin = Insets(4, 4, 4, 4))
    }

    /**
     * Läd eine Kurswahl Datei
     */
    private fun loadKurswahlFile(file: File): Boolean {
        if (file.extension != FILETYPE_EXTENSION) return false

        if (file.exists() && file.canRead()) {
            val mirror = FachDataMirror(if (this::fachData.isInitialized) fachData else null, SchoolConfig::getSchool)

            val mapper = jacksonObjectMapper()
            mapper.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
            mapper.injectableValues = InjectableValues.Std().addValue(FachDataMirror::class.java, mirror)

            try {
                val data = mapper.readValue(file, KurswahlData::class.java)
                val newFachData: FachData = mirror.fachData!!
                when {
                    data.readJsonVersion.first != newFachData.jsonVersion.first -> {
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist inkompatibel! Es tut uns leid, aber du musst deine Wahl erneut eingeben!",
                            "Inkompatible Datei",
                            JOptionPane.ERROR_MESSAGE
                        )
                        return false
                    }
                    data.readJsonVersion.second > newFachData.jsonVersion.second ->
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist neuer als die des Programms! Unter Umständen gehen ein paar Daten verloren!",
                            "Versionsunterschiede",
                            JOptionPane.WARNING_MESSAGE
                        )
                    data.readJsonVersion.second < newFachData.jsonVersion.second ->
                        JOptionPane.showMessageDialog(
                            this,
                            "Die Version deiner Datei ist älter als die des Programms! Unter Umständen müssen ein paar Daten neu eingetragen werden!",
                            "Versionsunterschiede",
                            JOptionPane.WARNING_MESSAGE
                        )
                }
                updateFachData(newFachData)
                wahlData = data
                data.updatePflichtfaecher()
                return true
            } catch (e: DatabindException) {
                JOptionPane.showMessageDialog(
                    this,
                    "Die Datei konnte nicht gelesen werden! Es tut uns leid, aber du musst deine Wahl erneut eingeben!",
                    "Fehlerhafte Datei",
                    JOptionPane.ERROR_MESSAGE
                )
                e.printStackTrace()
            } catch (e: MissingKotlinParameterException) {
                JOptionPane.showMessageDialog(
                    this,
                    "Die Datei konnte nicht gelesen werden! Es tut uns leid, aber du musst deine Wahl erneut eingeben!",
                    "Fehlerhafte Datei",
                    JOptionPane.ERROR_MESSAGE
                )
                e.printStackTrace()
            } catch (e: RuntimeException) {
                showLoadingError()
            }
        }

        return false
    }

    private fun openKurswahlAction() {
        val chooser = JFileChooser()
        chooser.fileFilter = KurswahlFileFilter
        chooser.dialogTitle = "Kurswahl-Datei öffnen"

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
                "Dein Wahl ist noch unvollständig oder ungültig! Vervollständige deine Wahl, bevor du sie exportieren kannst",
                "Ungültige Wahl",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        val data = curPanel.close()
        data.lock()

        val todos = data.check()

        if (todos != null) {
            JOptionPane.showMessageDialog(this, todos, "Ungültige Wahl", JOptionPane.ERROR_MESSAGE)
        } else if (data.countCourses().sum() !in fachData.minKurse..fachData.maxKurse) {
            JOptionPane.showMessageDialog(
                this,
                "Du hast noch nicht genügend Grundkurse! Es tut uns leid, aber du musst deine Wahl vervollständigen, bevor du sie exportieren kannst",
                "Ungültige Wahl",
                JOptionPane.ERROR_MESSAGE
            )
        } else if (fachData.regeln.all { it.match(data) }) {
            val chooser = JFileChooser()
            chooser.fileFilter = KurswahlFileFilter
            chooser.dialogTitle = "Datei für den Oberstufenkoordinator speichern"
            chooser.selectedFile =
                File(
                    "${data.vorname!!.split(' ')[0]}_${data.nachname}"
                        .replace(Regex("[\\\\/:*?\"<>|.&$]"), "")
                        .replace(' ', '_') + ".$FILETYPE_EXTENSION"
                )

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                val f =
                    chooser.selectedFile.let { File(it.parent, "${it.nameWithoutExtension}.$FILETYPE_EXTENSION") }

                thread {
                    try {
                        f.createNewFile()

                        jacksonObjectMapper().writer()
                            .withAttribute("jsonVersion", fachData.jsonVersion.let { "${it.first}.${it.second}" })
                            .writeValue(f, data)
                    } catch (exception: SecurityException) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Du hast keine Berechtigung diese Datei zu schreiben! Versuche einen anderen Namen oder Ordner!",
                            "Keine Rechte", JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }

            // speichern des Bilds der Kurswahl
            if (chooser.selectedFile != null)
                chooser.selectedFile =
                    File(chooser.currentDirectory, chooser.selectedFile.nameWithoutExtension + ".png")
            chooser.resetChoosableFileFilters()
            chooser.fileFilter = PngFileFilter
            chooser.dialogTitle = "Bild für dich speichern"


            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                val f =
                    File("${chooser.selectedFile.parent}${File.separatorChar}${chooser.selectedFile.nameWithoutExtension}.png")

                thread {
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
            }


        } else JOptionPane.showMessageDialog(
            this,
            "Deine Wahl erfüllt nicht alle Regeln! Es tut uns leid, aber du musst deine Wahl vervollständigen, bevor du sie exportieren kannst!",
            "Ungültige Wahl", JOptionPane.ERROR_MESSAGE
        )

        data.unlock()
    }

    /**
     * Benachrichtigt den Nutzer über einen Fehler beim Laden der Schul-Daten von GitHub
     */
    private fun showLoadingError() {
        val githubStatus = GithubStatus.get()
        val msg =
            if (githubStatus == null) "Stelle sicher das du eine Internet-Verbindung hast und versuche es erneut!"
            else if (githubStatus.indicator == GithubStatus.Companion.Status.NONE) "Scheint als sei ein Fehler beim Laden aufgetreten! Versuche es erneut!"
            else "GitHub's Server sind nicht erreichbar, da lässt sich nicht viel machen! Siehe: githubstatus.com"

        JOptionPane.showMessageDialog(
            this,
            "Die Daten für deine Schule konnten nicht geladen werden! " +
                    msg,
            "Fehler beim Laden der Datei!",
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
        schoolList.selectedIndex =
            if (currentSchool == null) 0
            else listModel.indexOf(currentSchool)

        schoolList.cellRenderer = SchoolRenderer()
        val pane = JScrollPane(schoolList)
        pane.preferredSize = Dimension(242, 298)

        val layout = JPanel(GridBagLayout())
        layout.add(pane, row = 0, column = 0, columnspan = 2, weightx = 1.0)

        val dialog = JDialog(null as JDialog?, "Schule wählen!", true)
        var result = -1

        val okBtn = JButton("OK")
        val btnDim = Dimension(85, 25)
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
                margin = Insets(9, 0, 2, 0),
                weightx = 1.0
            )
            layout.add(JButton("Abbrechen").apply {
                this.preferredSize = btnDim
                this.addActionListener {
                    result = JOptionPane.CANCEL_OPTION
                    dialog.dispose()
                }
            }, row = 1, column = 1, anchor = GridBagConstraints.WEST, margin = Insets(9, 0, 2, 0), weightx = 1.0)
        } else layout.add(okBtn, row = 1, column = 0, columnspan = 2, margin = Insets(9, 0, 2, 0))

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
        if (wahlData.fremdsprachen.size < 2 || wahlData.wpfs == null)
            for (i in 2..4) sidebarBtns[i].isEnabled = false
        else if (wahlData.lk1 == null || wahlData.lk2 == null) {
            sidebarBtns[3].isEnabled = false
            sidebarBtns[4].isEnabled = false
        } else if (wahlData.pf3 == null || wahlData.pf4 == null || wahlData.pf5 == null)
            sidebarBtns[4].isEnabled = false
        else for (i in 2..4) sidebarBtns[i].isEnabled = true
    }
}

