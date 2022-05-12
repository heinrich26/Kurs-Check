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

import java.awt.*
import javax.swing.*
import kotlin.Any
import kotlin.Array
import kotlin.String
import kotlin.arrayOf

class GridLayoutDemo(name: String?) : JFrame(name) {
    var horGapComboBox: JComboBox<*>? = null
    var verGapComboBox: JComboBox<*>? = null
    var applyButton = JButton("Apply gaps")
    var experimentLayout = GridBagLayout()

    init {
        isResizable = false
    }

    fun initGaps() {
        horGapComboBox = JComboBox<Any?>(gapList)
        verGapComboBox = JComboBox<Any?>(gapList)
    }

    fun addComponentsToPane(pane: Container) {
        initGaps()
        val compsToExperiment = JPanel()
        compsToExperiment.layout = experimentLayout
        val controls = JPanel()
        controls.layout = GridLayout(2, 3)

        //Set up components preferred size
        val b = JButton("Just fake button")
        val buttonSize = b.preferredSize
        compsToExperiment.preferredSize = Dimension(
            (buttonSize.getWidth() * 2.5).toInt() + maxGap,
            (buttonSize.getHeight() * 3.5).toInt() + maxGap * 2
        )

        //Add buttons to experiment with Grid Layout
        compsToExperiment.add(JLabel("KurswahlApp"))
        compsToExperiment.add(JButton("Button 1"))
        compsToExperiment.add(JButton("Button 2"), GridBagConstraints().apply { this.gridx = 0
        this.gridy=1})
        compsToExperiment.add(JButton("Button 3"), GridBagConstraints().apply { this.gridx = 0
        this.gridy=2})
        compsToExperiment.add(JButton("Long-Named Button 4"), GridBagConstraints().apply { this.gridy = 2
            this.gridx = 0
            this.gridwidth = 2 })
        compsToExperiment.add(JButton("5"))

        //Add controls to set up horizontal and vertical gaps
        controls.add(Label("Horizontal gap:"))
        controls.add(Label("Vertical gap:"))
        controls.add(Label(" "))
        controls.add(horGapComboBox)
        controls.add(verGapComboBox)
        controls.add(applyButton)

        //Process the Apply gaps button press
        applyButton.addActionListener { //Get the horizontal gap value
            val horGap = horGapComboBox!!.selectedItem as String
            //Get the vertical gap value
            val verGap = verGapComboBox!!.selectedItem as String
            //Set up the horizontal gap value
            experimentLayout.hgap = horGap.toInt()
            //Set up the vertical gap value
            experimentLayout.vgap = verGap.toInt()
            //Set up the layout of the buttons
            experimentLayout.layoutContainer(compsToExperiment)
        }
        pane.add(compsToExperiment, BorderLayout.NORTH)
        pane.add(JSeparator(), BorderLayout.CENTER)
        pane.add(controls, BorderLayout.SOUTH)
    }

    companion object {
        val gapList = arrayOf<String?>("0", "10", "15", "20")
        const val maxGap = 20

        /**
         * Create the GUI and show it.  For thread safety,
         * this method is invoked from the
         * event dispatch thread.
         */
        private fun createAndShowGUI() {
            //Create and set up the window.
            val frame = GridLayoutDemo("GridLayoutDemo")
            frame.defaultCloseOperation = EXIT_ON_CLOSE
            //Set up the content pane.
            frame.addComponentsToPane(frame.contentPane)
            //Display the window.
            frame.pack()
            frame.isVisible = true
        }

        @JvmStatic
        fun main(args: Array<String>) {
            /* Use an appropriate Look and Feel */
            try {
                //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel")
            } catch (ex: UnsupportedLookAndFeelException) {
                ex.printStackTrace()
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            } catch (ex: InstantiationException) {
                ex.printStackTrace()
            } catch (ex: ClassNotFoundException) {
                ex.printStackTrace()
            }
            /* Turn off metal's use of bold fonts */UIManager.put("swing.boldMetal", false)

            //Schedule a job for the event dispatch thread:
            //creating and showing this application's GUI.
            SwingUtilities.invokeLater { createAndShowGUI() }
        }
    }
}
