package gui

import java.awt.*
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.Icon
import javax.swing.JComponent


/**
 * The TextIcon will paint a String of text as an Icon. The Icon
 * can be used by any Swing component that supports icons.
 *
 * TextIcon supports two different layout styles:
 *
 *  * Horizontally - does normal rendering of the text by using the
 * Graphics.drawString(...) method
 *  * Vertically - Each character is displayed on a separate line
 *
 *
 * TextIcon was designed to be rendered on a specific JComponent as it
 * requires FontMetrics information in order to calculate its size and to do
 * the rendering. Therefore, it should only be added to component it was
 * created for.
 *
 * By default the text will be rendered using the Font and foreground color
 * of its associated component. However, this class does allow you to override
 * these properties. Also starting in JDK6 the desktop renderering hints will
 * be used to renderer the text. For versions not supporting the rendering
 * hints antialiasing will be turned on.
 */
class TextIcon(private val component: JComponent, char: Char) : Icon, PropertyChangeListener {

    /**
     * The text to be Rendered on the Icon
     */
    var char: Char = char
        set(value) {
            field = value
            calculateIconDimensions()
        }

    /**
     * The Font used to render the text. This will default to the Font
     * of the component unless the Font has been overridden.
     *
     */
    var font: Font? = null
        get() = field ?: component.font!!
        set(value) {
            field = value
            calculateIconDimensions()
        }

    /**
     * The foreground Color used to render the text. This will default to
     * the foreground Color of the component unless the foreground Color has
     * been overridden.
     */
    var foreground: Color? = null
        get() = field ?: component.foreground!!
        set(foreground) {
            field = foreground
            component.repaint()
        }

    //  Used for the implementation of Icon interface
    private var iconWidth = 0
    private var iconHeight = 0


    /**
     * The padding used when rendering the text, specified in pixels
     *
     * By default the size of the Icon is based on the size of the rendered
     * text. You can specify some padding to be added to the start and end
     * of the text when it is rendered.
     */
    var padding: Int = 0
        set(value) {
            field = value
            calculateIconDimensions()
        }

    init {
        component.addPropertyChangeListener("font", this)
    }

    /**
     * Calculate the size of the Icon using the FontMetrics of the Font.
     */
    private fun calculateIconDimensions() {
        val font = font
        val fm = component.getFontMetrics(font)
        iconWidth = fm.charWidth(char) + padding * 2
        iconHeight = fm.height

        component.revalidate()
    }
    //
    //  Implement the Icon Interface
    //
    /**
     * Gets the width of this icon.
     *
     * @return the width of the icon in pixels.
     */
    override fun getIconWidth(): Int {
        return iconWidth
    }

    /**
     * Gets the height of this icon.
     *
     * @return the height of the icon in pixels.
     */
    override fun getIconHeight(): Int {
        return iconHeight
    }

    /**
     * Paint the icons of this compound icon at the specified location
     *
     * @param c The component to which the icon is added
     * @param g the graphics context
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        val g2 = g.create() as Graphics2D

        //  The "desktophints" is supported in JDK6
        val toolkit = Toolkit.getDefaultToolkit()
        val map = toolkit.getDesktopProperty("awt.font.desktophints") as Map<*, *>
        g2.addRenderingHints(map)
        g2.font = font
        g2.color = foreground
        val fm = g2.fontMetrics
        g2.translate(x, y + fm.ascent)
        g2.drawString(char.toString(), padding, 0)
        g2.dispose()
    }

    //
    //  Implement the PropertyChangeListener interface
    //
    override fun propertyChange(e: PropertyChangeEvent) {
        //  Handle font change when using the default font
        if (font == null) calculateIconDimensions()
    }
}