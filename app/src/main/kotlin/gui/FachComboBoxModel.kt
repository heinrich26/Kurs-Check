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

package com.kurswahlApp.gui

import com.kurswahlApp.data.Fach
import java.io.Serializable
import java.util.*
import javax.swing.AbstractListModel
import javax.swing.DefaultComboBoxModel
import javax.swing.MutableComboBoxModel

/**
 * Eine Implementation des [MutableComboBoxModel]s, welche addAll() in Java 1.8 unterstützt
 *
 * @author Arnaud Weber
 * @author Tom Santos
 * @author Hendrik Horstmann
 */
@Suppress("unused")
open class FachComboBoxModel : AbstractListModel<Fach?>, MutableComboBoxModel<Fach?>, Serializable {
    private var objects: Vector<Fach?>
    private var selectedObject: Fach? = null

    /**
     * Constructs an empty FachComboBoxModel object.
     */
    constructor() {
        objects = Vector()
    }

    /**
     * Constructs a FachComboBoxModel object prefilled with
     * an array of [items].
     */
    constructor(items: Array<Fach?>) {
        objects = Vector(items.size)
        objects.addAll(items)
        if (size > 0) {
            selectedObject = getElementAt(0)
        }
    }

    /**
     * Constructs a FachComboBoxModel object prefilled with
     * a [vector].
     */
    constructor(vector: Vector<Fach?>) {
        objects = vector
        if (size > 0) {
            selectedObject = getElementAt(0)
        }
    }

    /**
     * Constructs a FachComboBoxModel object prefilled with
     * a collection of [items].
     */
    constructor(items: Collection<Fach?>) {
        objects = Vector(items)
        if (size > 0) {
            selectedObject = getElementAt(0)
        }
    }
    // implements javax.swing.ComboBoxModel
    /**
     * Set the value of the selected item to [anObject]. The selected item may be null.
     */
    override fun setSelectedItem(anObject: Any?) {
        if (anObject is Fach? && (selectedObject != null && selectedObject != anObject ||
            selectedObject == null && anObject != null)
        ) {
            selectedObject = anObject
            fireContentsChanged(this, -1, -1)
        }
    }

    // implements javax.swing.ComboBoxModel
    override fun getSelectedItem(): Fach? = selectedObject

    // implements javax.swing.ListModel
    override fun getSize(): Int = objects.size

    // implements javax.swing.ListModel
    final override fun getElementAt(index: Int): Fach? =
        if (index >= 0 && index < objects.size) objects.elementAt(index) else null


    /**
     * Returns the index-position of [anObject] in the list.
     */
    fun getIndexOf(anObject: Any?): Int {
        return objects.indexOf(anObject)
    }

    // implements javax.swing.MutableComboBoxModel
    override fun addElement(anObject: Fach?) {
        objects.addElement(anObject)
        fireIntervalAdded(this, objects.size - 1, objects.size - 1)
        if (objects.size == 1 && selectedObject == null && anObject != null) {
            selectedItem = anObject
        }
    }


    /**
     * Fügt alle [items] in das ComboBoxModell ein.
     *
     * Kopiert aus Corretto-16 [DefaultComboBoxModel.addAll] für 1.8 Kompatibilität
     */
    fun addAll(items: Collection<Fach>) {
        if (items.isEmpty()) return

        val startIndex = size

        objects.addAll(items)
        fireIntervalAdded(this, startIndex, size - 1)
    }

    // implements javax.swing.MutableComboBoxModel
    override fun insertElementAt(anObject: Fach?, index: Int) {
        objects.insertElementAt(anObject, index)
        fireIntervalAdded(this, index, index)
    }

    // implements javax.swing.MutableComboBoxModel
    override fun removeElementAt(index: Int) {
        if (getElementAt(index) === selectedObject) {
            selectedItem = if (index == 0) {
                if (size == 1) null else getElementAt(1)
            } else getElementAt(index - 1)
        }
        objects.removeElementAt(index)
        fireIntervalRemoved(this, index, index)
    }

    // implements javax.swing.MutableComboBoxModel
    override fun removeElement(anObject: Any?) {
        if (anObject !is Fach?) return

        val index = objects.indexOf(anObject)
        if (index != -1) {
            removeElementAt(index)
        }
    }

    /**
     * Empties the list.
     */
    fun removeAllElements() {
        if (objects.size > 0) {
            val firstIndex = 0
            val lastIndex = objects.size - 1
            objects.removeAllElements()
            selectedObject = null
            fireIntervalRemoved(this, firstIndex, lastIndex)
        } else {
            selectedObject = null
        }
    }

    /**
     * Empties the list.
     */
    fun clear() = removeAllElements()
}