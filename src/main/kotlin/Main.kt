// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import data.KurswahlData
import javax.swing.JFrame

fun setupApp(): JFrame {
    val frame = JFrame("kurswahlApp")
    frame.contentPane = overviewWidget().mainPanel
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(400, 400)
    frame.isResizable = false
    frame.setLocationRelativeTo(null)
    frame.isVisible = true

    return frame
}

fun main(args: Array<String>) {
    val data = KurswahlData()
    data.pf3 = Fach("Französisch", "F", 2)
    val frame = setupApp()
}
