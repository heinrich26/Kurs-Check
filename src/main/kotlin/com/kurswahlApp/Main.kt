package com.kurswahlApp

import com.kurswahlApp.gui.GuiMain
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional


fun main(args: Array<String>) {
    val parser = ArgParser("kurswahlApp")

    val input by parser.argument(
        ArgType.String, "input",
        "Die zum öffnen verwendete Datei oder der für die CSV-Generierung verwendete Ordner"
    ).optional()

    val csvGen by parser.option(
        ArgType.Boolean, "merge", "m",
        "Ob das Tool zur Zusammenführung von Kurswahldaten ausgeführt werden soll"
    ).default(false)

    val useTestData by parser.option(ArgType.Boolean, "useTestData", description = "Testdaten verwenden").default(false)

    parser.parse(args)

    if (csvGen)
        com.kurswahlApp.csvGenerator.main(input) // CLI merger starten
    else
        GuiMain.run(input, useTestData) // GUI App starten
}