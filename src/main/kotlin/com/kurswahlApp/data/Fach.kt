package com.kurswahlApp.data

import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * Beschreibt ein Fach
 *
 * @constructor Erstellt ein Fach
 * @property name der Anzeigename des Fachs
 * @property kuerzel eindeutiges Schlüsselattribut für dieses Fachs;
 * Wird verwendet um Instanzen zu vergleichen
 * @property aufgabenfeld das Aufgabenfeld, dem das Fach angehört
 * @property isLk ob das [Fach] als LK gewählt werden kann
 * @property isFremdsprache ob das [Fach] eine Fremdsprache ist
 * @property isKurs ob das Fach als Kurs gewählt werden kann
 * @property brauchtWPF ob SuS das [Fach] as WPF belegt haben müssen
 * @property nurPf4_5 ob das [Fach] nur als 4./5. PF gewählt werden kann
 * @property nurIn beschränkt, in welchen Semestern das Fach gewählt werden kann
 */
@JsonSerialize(using = FachSerializer::class, keyUsing = FachKeySerializer::class)
data class Fach(
    val name: String,
    val kuerzel: String,
    val aufgabenfeld: Int,
    val isLk: Boolean = false,
    val isFremdsprache: Boolean = false,
    val isKurs: Boolean = true,
    val brauchtWPF: Boolean = false,
    val nurPf4_5: Boolean = false,
    val nurIn: Wahlmoeglichkeit = Wahlmoeglichkeit.DURCHGEHEND
) {
    /**
     * Gibt den Namen des [Fach]s mit, wenn vorhanden, dem Aufgabenfeld zurück
     *
     * Nach dem Schema: `<Fach.name> (<Fach.aufgabenfeld>)`
     */
    fun nameFormatted(): String = if (aufgabenfeld < 1) name else "$name ($aufgabenfeld)"

    override fun equals(other: Any?): Boolean = this === other || (other is Fach && this.kuerzel == other.kuerzel)

    override fun hashCode(): Int = kuerzel.hashCode() // nimmt an, dass das selbe Kürzel nur 1x vorkommt
}
