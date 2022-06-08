package gui

import add
import data.Fach
import data.FachComboBoxModel
import data.FachData
import data.KurswahlData
import data.WahlzeileLinientyp.*
import testFachdata
import testKurswahl
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Box
import javax.swing.JLabel


class Pruefungsfaecher(wahlData: KurswahlData, fachData: FachData) : KurswahlPanel(wahlData, fachData) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runTest { Pruefungsfaecher(testKurswahl, testFachdata) }
        }

        class AwareFachComboBoxModel(vararg vorgaenger: FachComboBox, generator: () -> Collection<Fach>) :
            FachComboBoxModel(generator()) {
            init {
                for (comboBox in vorgaenger) {
                    comboBox.addActionListener {
                        this.removeAllElements()
                        this.addAll(generator())
                    }
                }
            }
        }
    }

    private val pf3: FachComboBox
    private val pf4: FachComboBox
    private val pf5: FachComboBox

    private val userFs = wahlData.fremdsprachen.map { it.first }
    private val userWpfs = wahlData.wpfs
    private val filteredZeilen = fachData.filterWahlzeilen(wahlData.lk1, wahlData.lk2)
    private val filteredFaecher = fachData.faecherMap.filterValues {
        /* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 */
        it != wahlData.lk1 && it != wahlData.lk2 && if (it.fremdsprache) it in userFs
        /* Hat keine WPF or Fach ist weder 1./2. WPF */
        else (!it.brauchtWPF || (userWpfs != null && (it == userWpfs.first || it == userWpfs.second)))
    } // TODO nur PFs returnen, Fächer evtl. sowieso vorfiltern

    init {
        this.layout = GridBagLayout()

        add(
            JLabel("Prüfungsfächer:"),
            column = 0,
            columnspan = 2,
            margin = Insets(0, 0, 4, 0),
            anchor = GridBagConstraints.WEST
        )
        add(Box.createHorizontalStrut(50), column = 2)

        println(filteredZeilen)
        val pf3faecher = faecherAusWahlzeilen(3)

        // geht schon
        val model1 = FachComboBoxModel(pf3faecher)
        pf3 = FachComboBox(model1)

        // wip
        val model2 = AwareFachComboBoxModel(pf3) { faecherAusWahlzeilen(4) }
        pf4 = FachComboBox(model2)

        val model3 = AwareFachComboBoxModel(pf3, pf4) { faecherAusWahlzeilen(5) }
        pf5 = FachComboBox(model3)


        pf3.renderer = FachRenderer
        pf4.renderer = FachRenderer
        pf5.renderer = FachRenderer


        // Daten einsetzen
        pf3.selectedItem = wahlData.pf3
        pf4.selectedItem = wahlData.pf4
        pf5.selectedItem = wahlData.pf5


        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(pf3, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf4, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf5, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
        }

        // Beschriftungen hinzufügen
        for (i in 3..5) {
            add(JLabel("PF $i."), row = i - 2, column = 0)
        }
    }

    private fun pf3Faecher(): Collection<Fach> {
        // TODO Performance von LinkedHashSet und MutableSet vergleichen
        val kuerzel = mutableSetOf<String>()

        for (wz in filteredZeilen.values) {
            if (wz.pf3 == "*") return filteredFaecher.values

            if (wz.pf3.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf3]!!)
            else kuerzel.add(wz.pf3)

            if (wz.linien != DURCHGEZOGEN) {
                if (wz.pf4 == "*") return filteredFaecher.values

                if (wz.pf4.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                else kuerzel.add(wz.pf4)

                /*if (wz.linien == KEINE) {*/
                if (wz.pf5 == "*") return filteredFaecher.values

                if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                else kuerzel.add(wz.pf5)
                /*}*/
            }
        }

        return kuerzel.mapNotNull { filteredFaecher[it] }
    }

    private fun pf4Faecher(): List<Fach> {
        val selectedPf3 = pf3.selectedItem ?: return emptyList()
        val pf3Groups = fachData.wzWildcardMapping[selectedPf3]!!

        val beliebig: () -> List<Fach> = {
            if (selectedPf3.kuerzel in fachData.pf3_4AusschlussFaecher)
                filteredFaecher.values.filter { it.kuerzel !in fachData.pf3_4AusschlussFaecher }
            else filteredFaecher.values.filter { it != selectedPf3 }
        }

        val kuerzel = mutableSetOf<String>()
        for (wz in filteredZeilen.values) {
            if (wz.pf3 in pf3Groups) {
                if (wz.pf4 == "*") return beliebig()

                if (wz.pf4.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                else kuerzel.add(wz.pf4)

                if (wz.linien == GESTRICHELT || wz.linien == KEINE) {
                    if (wz.pf5 == "*") return beliebig()

                    if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                    else kuerzel.add(wz.pf5)
                }
            }

            if (wz.linien != DURCHGEZOGEN) {
                if (wz.pf4 in pf3Groups) {
                    if (wz.pf3 == "*") return beliebig()

                    if (wz.pf3.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf3]!!)
                    else kuerzel.add(wz.pf3)

                    if (wz.linien == KEINE) {
                        if (wz.pf5 == "*") return beliebig()

                        if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                        else kuerzel.add(wz.pf5)
                    }
                }

                if (wz.pf5 in pf3Groups) {
                    if (wz.pf3 == "*") return beliebig()

                    if (wz.linien == KEINE) {
                        if (wz.pf4 == "*") return beliebig()

                        if (wz.pf4.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                        else kuerzel.add(wz.pf4)
                    }

                    if (wz.pf3.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf3]!!)
                    else kuerzel.add(wz.pf3)
                }
            }
        }

        if (selectedPf3.kuerzel in fachData.pf3_4AusschlussFaecher) kuerzel.removeAll(fachData.pf3_4AusschlussFaecher)
        kuerzel.remove(selectedPf3.kuerzel) // Prüfungsfach 3 nicht 2x wählen
        return kuerzel.mapNotNull { filteredFaecher[it] }
    }

    private fun pf5Faecher(): List<Fach> {
        val selectedPf3 = pf3.selectedItem ?: return emptyList()
        val pf3Groups = fachData.wzWildcardMapping[selectedPf3]!!

        val selectedPf4 = pf4.selectedItem ?: return emptyList()
        val pf4Groups = fachData.wzWildcardMapping[selectedPf4]!!

        val beliebig: () -> List<Fach> = {
            if (selectedPf3.kuerzel in fachData.pf3_4AusschlussFaecher || selectedPf4.kuerzel in fachData.pf3_4AusschlussFaecher)
                filteredFaecher.values.filter { it.kuerzel !in fachData.pf3_4AusschlussFaecher }
            else filteredFaecher.values.filter { it != selectedPf3 && it != selectedPf4 }
        }

        val kuerzel = mutableSetOf<String>()

        for (wz in filteredZeilen.values) {
            if (wz.pf3 in pf3Groups && wz.pf4 in pf4Groups) {
                if (wz.pf5 == "*") return beliebig()

                if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                else kuerzel.add(wz.pf5)
            }

            if (wz.linien != DURCHGEZOGEN) {
                if (wz.pf4 in pf3Groups && wz.pf3 in pf4Groups) {
                    if (wz.pf5 == "*") return beliebig()

                    if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                    else kuerzel.add(wz.pf5)
                }

                if (wz.linien != KEINE_DURCHGEZOGEN) {
                    if ((wz.pf3 in pf3Groups && wz.pf5 in pf4Groups) || (wz.pf3 in pf4Groups && wz.pf5 in pf3Groups)) {
                        if (wz.pf4 == "*") return beliebig()

                        if (wz.pf4.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                        else kuerzel.add(wz.pf4)
                    }

                    if ((wz.pf4 in pf3Groups && wz.pf5 in pf4Groups) || (wz.pf4 in pf4Groups && wz.pf5 in pf3Groups)) {
                        if (wz.pf3 == "*") return beliebig()

                        if (wz.pf3.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf3]!!)
                        else kuerzel.add(wz.pf3)
                    }
                }
            }
        }

        if (selectedPf3.kuerzel in fachData.pf3_4AusschlussFaecher || selectedPf4.kuerzel in fachData.pf3_4AusschlussFaecher)
            kuerzel.removeAll(fachData.pf3_4AusschlussFaecher)
        kuerzel.remove(selectedPf3.kuerzel) // Prüfungsfach 3 nicht 2x wählen
        kuerzel.remove(selectedPf4.kuerzel) // Prüfungsfach 4 nicht 2x wählen
        return kuerzel.mapNotNull { filteredFaecher[it] }
    }

    /**
     * Gibt die Fächer, die die gegebenen Wahlzeilen zulassen zurück
     * TODO funzt noch nicht so ganz
     */
    private fun faecherAusWahlzeilen(pf: Int): Collection<Fach> {
        return when (pf) {
            3 -> pf3Faecher()
            4 -> pf4Faecher()
            else -> pf5Faecher()
        }/*.filter {
            *//* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 *//*
            if (it.fremdsprache) it in fremdsprachen
            *//* Hat keine WPF or Fach ist weder 1./2. WPF *//*
            else (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second)))
        } // TODO nur PFs returnen, Fächer evtl. sowieso vorfiltern*/
    }


    /*LinkedHashSet(wahlzeilen.values.flatMap { wz ->
        val kuerzel = when (pf) {
            3 -> wz.pf3
            4 -> wz.pf4
            else -> wz.pf5
        }

        if (kuerzel == "*") fachData.faecher else
        if (kuerzel.startsWith("$")) fachData.wzWildcards[kuerzel]!!
        else Collections.singleton(fachData.faecherMap[kuerzel]!!)
    }).filter {
        *//* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 *//*
            if (it.fremdsprache) it in fremdsprachen
            *//* Hat keine WPF or Fach ist weder 1./2. WPF *//*
            else (!it.brauchtWPF || (wpfs != null && (it == wpfs.first || it == wpfs.second)))
        }*/

    override fun close(): KurswahlData =
        wahlData.updatePFs(pf3 = pf3.selectedItem!!, pf4 = pf4.selectedItem!!, pf5 = pf5.selectedItem!!)

    override fun isDataValid(): Boolean =
        (pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null)

    override val windowName: String
        get() = "Prüfungsfächer"
}