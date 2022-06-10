package com.kurswahlApp.gui

import com.kurswahlApp.add
import com.kurswahlApp.data.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ItemEvent
import javax.swing.Box


class Leistungskurse(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Leistungskurse(testKurswahl, testFachdata) }
        }

        class LKComboBoxModel(data: Collection<Fach>, other: FachComboBox) : FachComboBoxModel(data = data) {
            private var excludedItem: Pair<Fach, Int>? = null

            init {
                other.addItemListener {
                    if (it.stateChange == ItemEvent.SELECTED) {
                        if (excludedItem != null) {
                            insertElementAt(excludedItem!!.first, excludedItem!!.second)
                            excludedItem = null
                        }
                        val item = (it.item ?: return@addItemListener) as Fach

                        if (selectedItem == item)
                            selectedItem = null
                        excludedItem = item to getIndexOf(item).also { i -> removeElementAt(i) }
                    }
                }
            }
        }
    }

    private val lk1: FachComboBox
    private val lk2: FachComboBox

    init {
        layout = GridBagLayout()


        add(Box.createHorizontalStrut(50), column = 2)


        // Eine Fremdsprache, die erst in der Jahrgangsstufe 10 oder in der Einführungsphase begonnen wurde,
        // darf nur als 3. oder 4. Prüfungsfach oder als Referenzfach der 5. PK gewählt werden.
        val fs = wahlData.fremdsprachen.mapNotNull { (fach, jahr) -> if (jahr >= 10) null else fach }

        val model1 = FachComboBoxModel(fachData.lk1Moeglichkeiten.filter { !it.fremdsprache || it in fs })
        lk1 = FachComboBox(model1)


        val wpfs = wahlData.wpfs
        val moeglichkeiten = fachData.lk2Moeglichkeiten.filter {
            /* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 */
            if (it.fremdsprache) it in fs
            /* Hat keine WPF or Fach ist weder 1./2. WPF */
            else (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second)))
        }
        val model2 = LKComboBoxModel(moeglichkeiten, lk1)
        lk2 = FachComboBox(model2)
        val listener: (Any) -> Unit = { notifier.invoke(lk2.selectedItem != null) }
        lk1.addActionListener(listener)
        lk2.addActionListener(listener)
        listener.invoke(Any())


        lk1.renderer = FachRenderer
        lk2.renderer = FachRenderer


        // Daten einsetzen
        lk1.selectedItem = wahlData.lk1
        lk2.selectedItem = wahlData.lk2

        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(lk1, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(lk2, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }
    }


    override fun close(): KurswahlData = wahlData.updateLKs(lk1 = lk1.selectedItem!!, lk2 = lk2.selectedItem!!)

    override fun isDataValid(): Boolean = (lk1.selectedItem != null && lk2.selectedItem != null)

    override val windowName: String
        get() = "Leistungskurse"
}