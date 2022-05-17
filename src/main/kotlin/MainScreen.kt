/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import data.Fach
import data.KurswahlData
import data.Wahlmoeglichkeit
import java.awt.*
import javax.swing.*
import kotlin.String

class MainScreen(name: String?) : JFrame(name) {
    var layout = GridBagLayout()

    init {
        isResizable = false
    }

    fun addComponentsToPane(pane: Container) {
        val panel = JPanel()
        panel.layout = layout
        val overview = WahlVisualizer(KurswahlData().apply {
            lk1 = faecher[0]
            lk2 = faecher[1]
            pf3 = faecher[2]
            pf4 = faecher[3]
            pf5 = faecher[4]
            gks = faecher.map { it to Wahlmoeglichkeit.DRITTES_VIERTES }
        })

        val printButton = JButton("Drucken")
        val editButton = JButton("Wahl ändern")

        //Add buttons to experiment with Grid Layout
        panel.add(
            JLabel("KurswahlApp").apply { this.font = Font(font.name, Font.BOLD, 24) },
            columnspan = 2,
            anchor = GridBagConstraints.WEST,
            margin = Insets(10, 10, 10, 10)
        )

        panel.add(printButton, column = 1, row = 1)
        panel.add(editButton, column = 1, row = 3)
        panel.add(
            overview,
            row = 1,
            column = 0,
            rowspan = 3,
            weighty = 1.0,
            anchor = GridBagConstraints.NORTHWEST,
            margin = Insets(8, 8, 8, 8)
        )

        //Process the Print button press
        printButton.addActionListener {
            println("user möchte drucken!!!")
        }

        editButton.addActionListener {
            println("ändere wahl")
            TODO("Wahl Fenster einbauen")
        }

        pane.add(panel)
    }

    companion object {
        /**
         * Create the GUI and show it.  For thread safety,
         * this method is invoked from the
         * event dispatch thread.
         */
        fun createAndShowGUI() {
            //Create and set up the window.
            val frame = MainScreen("kurswahlApp")
            frame.defaultCloseOperation = EXIT_ON_CLOSE
            //Set up the content pane.
            frame.addComponentsToPane(frame.contentPane)
            //Display the window.
            frame.pack()
            frame.isVisible = true
        }

        @JvmStatic
        fun main(args: Array<String>) {
            /* Windows UI verwenden */
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            //Schedule a job for the event dispatch thread:
            //creating and showing this application's GUI.
            SwingUtilities.invokeLater { createAndShowGUI() }
        }
    }
}
