package com.kurswahlApp.gui

import com.kurswahlApp.add
import com.kurswahlApp.data.*
import com.kurswahlApp.data.WahlzeileLinientyp.*
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.JComboBox
import javax.swing.JLabel


class Pruefungsfaecher(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {

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

    @Suppress("PrivatePropertyName")
    private val pf5_typ: JComboBox<Pf5Typ>

    private val userFs = wahlData.fremdsprachen.map { it.first }
    private val userWpfs = wahlData.wpfs
    private val filteredZeilen = fachData.filterWahlzeilen(wahlData.lk1, wahlData.lk2)
    private val filteredFaecher = fachData.faecherMap.filterValues {
        // ist als Kurs wählbar
        it.isKurs &&
        // ist kein Zusatzkurs & ...
        it.aufgabenfeld != -1 &&
                /* Fach ist keine Fremdsprache bzw. Schüler hatte sie in Sek 1 */
                (it != wahlData.lk1 && it != wahlData.lk2 && if (it.isFremdsprache) it in userFs
                /* Hat keine WPF or Fach ist weder 1./2. WPF */
                else (!it.brauchtWPF || (userWpfs != null && (it == userWpfs.first || it == userWpfs.second))))
    }
    private val zeilenFuerFuenfte: MutableSet<Pair<Int, Wahlzeile>> = mutableSetOf()

    init {
        val pf3faecher = faecherAusWahlzeilen(3)

        // geht schon
        val model1 = FachComboBoxModel(pf3faecher)
        pf3 = FachComboBox(model1)

        // wip
        val model2 = AwareFachComboBoxModel(pf3) { faecherAusWahlzeilen(4) }
        pf4 = FachComboBox(model2)

        val model3 = AwareFachComboBoxModel(pf3, pf4) { faecherAusWahlzeilen(5) }
        pf5 = FachComboBox(model3)

        pf5_typ = JComboBox(Pf5Typ.values())


        pf3.renderer = FachRenderer
        pf4.renderer = FachRenderer
        pf5.renderer = FachRenderer
        pf5_typ.renderer = Pf5Typ.Renderer


        // Daten einsetzen
        pf3.selectedItem = wahlData.pf3
        pf4.selectedItem = wahlData.pf4
        pf5.selectedItem = wahlData.pf5
        pf5_typ.selectedItem = wahlData.pf5_typ

        pf3.addActionListener { notifier.invoke(pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null) }
        pf4.addActionListener { notifier.invoke(pf4.selectedItem != null && pf5.selectedItem != null) }
        pf5.addActionListener { notifier.invoke(pf5.selectedItem != null) }
        notifier.invoke(pf5.selectedItem != null)


        // Anzeigen
        // Margin hinzufügen
        Insets(1, 0, 1, 0).let {
            add(pf3, row = 1, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf4, row = 2, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf5, row = 3, column = 1, fill = GridBagConstraints.BOTH, margin = it)
            add(pf5_typ, row = 3, column = 3, margin = it)
        }

        // Beschriftungen hinzufügen
        for (i in 3..5) {
            add(JLabel("PF $i."), row = i - 2, column = 0)
        }
        add(JLabel(" als "), row = 3, column = 2)
    }

    private fun pf3Faecher(): Collection<Fach> {
        val kuerzel = mutableSetOf<String>()

        val beliebig: () -> List<Fach> = { filteredFaecher.mapNotNull { if (it.value.nurPf4_5) null else it.value } }

        for (wz in filteredZeilen.values) {
            if (wz.pf3 == "*") return beliebig()

            if (wz.pf3.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf3]!!)
            else kuerzel.add(wz.pf3)

            if (wz.linien != DURCHGEZOGEN) {
                if (wz.pf4 == "*") return beliebig()

                if (wz.pf4.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                else kuerzel.add(wz.pf4)

                if (wz.linien != KEINE_DURCHGEZOGEN) {
                    if (wz.pf5 == "*") return beliebig()

                    if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                    else kuerzel.add(wz.pf5)
                }
            }
        }

        return kuerzel.mapNotNull {
            val fach = filteredFaecher[it] ?: return@mapNotNull null
            if (fach.nurPf4_5) null else fach
        }
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

        zeilenFuerFuenfte.clear()

        for (wz in filteredZeilen.values) {
            if (wz.pf3 in pf3Groups && wz.pf4 in pf4Groups) {
                zeilenFuerFuenfte.add(5 to wz)
                if (wz.pf5 == "*") return beliebig()

                if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                else kuerzel.add(wz.pf5)
            }

            if (wz.linien != DURCHGEZOGEN) {
                if (wz.pf4 in pf3Groups && wz.pf3 in pf4Groups) {
                    zeilenFuerFuenfte.add(5 to wz)
                    if (wz.pf5 == "*") return beliebig()

                    if (wz.pf5.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf5]!!)
                    else kuerzel.add(wz.pf5)
                }

                if (wz.linien != KEINE_DURCHGEZOGEN) {
                    if ((wz.pf3 in pf3Groups && wz.pf5 in pf4Groups) || (wz.pf3 in pf4Groups && wz.pf5 in pf3Groups)) {
                        zeilenFuerFuenfte.add(4 to wz)
                        if (wz.pf4 == "*") return beliebig()

                        if (wz.pf4.startsWith("$")) kuerzel.addAll(fachData.wzWildcards[wz.pf4]!!)
                        else kuerzel.add(wz.pf4)
                    }

                    if ((wz.pf4 in pf3Groups && wz.pf5 in pf4Groups) || (wz.pf4 in pf4Groups && wz.pf5 in pf3Groups)) {
                        zeilenFuerFuenfte.add(3 to wz)
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

    override fun close(): KurswahlData {
        val selectedPf5 = pf5.selectedItem!!
        val pf5Groups = fachData.wzWildcardMapping[selectedPf5]!!
        var zeile: Int = -1
        for ((field, wz) in zeilenFuerFuenfte) {
            val fieldVal = when (field) {
                3 -> wz.pf3
                4 -> wz.pf4
                else -> wz.pf5
            }
            if (fieldVal in pf5Groups) {
                zeile = filteredZeilen.firstNotNullOf { if (it.value == wz) it.key else null }
                break
            }
        }

        return wahlData.updatePFs(
            pf3 = pf3.selectedItem!!,
            pf4 = pf4.selectedItem!!,
            pf5 = selectedPf5,
            pf5_typ = pf5_typ.selectedItem as Pf5Typ,
            wahlzeile = zeile
        )
    }

    override fun isDataValid(): Boolean =
        (pf3.selectedItem != null && pf4.selectedItem != null && pf5.selectedItem != null)

    override val windowName: String
        get() = "Prüfungsfächer"
}