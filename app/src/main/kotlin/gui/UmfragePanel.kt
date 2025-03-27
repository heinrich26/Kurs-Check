/*
 * Copyright (c) 2025  Hendrik Horstmann
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

import com.kurswahlApp.R
import com.kurswahlApp.data.*
import gui.TitledPanel
import java.awt.GridBagConstraints.BOTH
import java.awt.GridBagConstraints.HORIZONTAL
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import kotlin.math.min

/* TODO
 * Hilfe zu den Umfragen
 * Korrektes Layout und Sizing
 * Scrollen bei Langen Umfragen
 */
class UmfragePanel(wahlData: KurswahlData, fachData: FachData, notifier: (Boolean) -> Unit = {}) :
    KurswahlPanel(wahlData, fachData, notifier) {
    private val umfragen: List<UmfrageComponent<*>>
    private val validities = BooleanArray(fachData.umfragen.size) { false }
    private fun handleUmfrageNotifiers() = notifier(this.isDataValid())

    init {
        this.maximumSize = 250 by Int.MAX_VALUE // bringt nix
        // dynamically construct choice parts
        umfragen = if (fachData.umfragen.isEmpty()) {
            add(JLabel("Es sind keine Umfragen vorhanden.<br>Wenn du das hier siehst, ist etwas schiefgelaufen.".wrappable()))
            emptyList()
        } else {
            fachData.umfragen.mapIndexed { i, umfrage ->
                umfrage.toPanel().constructors.first().call(umfrage, wahlData.umfrageData.getOrNull(i), { b: Boolean ->
                    validities[i] = b
                    handleUmfrageNotifiers()
                }).also { add(it, fill = HORIZONTAL, row = i) }
            }
        }
    }

    override fun close(): KurswahlData = wahlData.copy(umfrageData = umfragen.map(UmfrageComponent<*>::getData))

    override fun isDataValid(): Boolean = validities.all { it }

    override fun showHelp(): String = "ich hoffe das erklärt sich von selbst :)"

    override val windowName: String = "Umfragen"

    sealed class UmfrageComponent<T : Any>(
        val content: UmfrageBase<T>,
        value: T?,
        private val notifier: (Boolean) -> Unit
    ) : TitledPanel(content.title, radius = 8, layout = GridBagLayout()) {
        abstract fun isDataValid(): Boolean
        abstract fun getData(): T

        init {
            content.desc?.let { add(JLabel(it.wrappable(200)), columnspan = 3, fill = BOTH, margin = Insets(bottom = 8)) }
        }
    }


    class NumberRangeUmfrageComponent(
        content: NumberRangeUmfrage,
        value: Int?, notifier: (Boolean) -> Unit
    ) : UmfrageComponent<Int>(content, value, notifier) {
        private val model: SpinnerNumberModel = SpinnerNumberModel(
            value ?: content.range.first,
            content.range.first,
            content.range.last,
            1
        )

        init {
            val spinner = JSpinner(model)
            add(spinner, margin = Insets(left = 4))
            notifier(true)
        }

        override fun isDataValid(): Boolean = true

        override fun getData(): Int = model.number as Int
    }

    class PriorityUmfrageComponent(
        content: PriorityUmfrage,
        value: List<Int>?, umfrageNotifier: (Boolean) -> Unit
    ) : UmfrageComponent<List<Int>>(content, value, umfrageNotifier) {
        init {
            assert(this.content is PriorityUmfrage)
        }

        private val errorLabel: JLabel = JLabel().also {
            it.foreground = Consts.COLOR_ERROR
        }

        private val models: List<SpinnerNumberModel> = (value ?: listOfNulls<Int>(content.options.size)).map {
            SpinnerNumberModel(it ?: 0, 0, min(content.max, content.options.size), 1)
        }

        init {
            for ((text, model) in content.options.zip(models)) {
                model.addChangeListener { umfrageNotifier(this@PriorityUmfrageComponent.isDataValid()) }
                this.add(JLabel(text), fill = BOTH, column = 0, weightx = 1.0)
                this.add(JSpinner(model), column = 1, margin = Insets(left = 4))
            }
            add(errorLabel, column = 0, columnspan = 2, fill = BOTH, margin = Insets(top = 4))
            umfrageNotifier(isDataValid())
        }

        private val errorText = R.getString("prioUmfrageErrorMsg", content.min).wrappable(200)
        override fun isDataValid(): Boolean = (getData().count { it > 0 } >= (content as PriorityUmfrage).min &&
                getData().filter { it > 0 }.let { it.size == it.distinct().size }).also {
            errorLabel.text = if (it) "" else errorText
            errorLabel.invalidate()
        }

        override fun getData(): List<Int> = models.map { it.number as Int }
    }

    private companion object {
        fun UmfrageBase<*>.toPanel() = when (this) {
            is NumberRangeUmfrage -> NumberRangeUmfrageComponent::class
            is PriorityUmfrage -> PriorityUmfrageComponent::class
        }
    }
}