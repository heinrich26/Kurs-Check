package com.kurswahlApp.gui

import com.kurswahlApp.data.Fach
import java.io.Serializable
import java.util.*
import javax.swing.AbstractListModel
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
     */
    fun addAll(items: Collection<Fach>) {
        if (items.isEmpty()) return

        val oldSize = objects.size
        objects.addAll(items)
        fireIntervalAdded(this, oldSize, oldSize + items.size - 1)
        if (oldSize == 0 && selectedObject == null) {
            selectedItem = objects[0]
        }
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
                if (size == 1) null else getElementAt(index + 1)
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