/*
 * Copyright (c) 2022-2025  Hendrik Horstmann
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

import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent


/**
 *  Convenience class to create and optionally save to a file a
 *  [BufferedImage] of an area on the screen. Generally there are
 *  four different scenarios. Create an image of:
 *  - an entire component
 *  - a region of the component
 *  - the entire desktop
 *  - a region of the desktop
 *
 *  The first two use the Swing [Component.paint] method to draw the
 *  component image to the [BufferedImage]. The latter two use the
 *  AWT Robot to create the [BufferedImage].
 *
 *  The created image can then be saved to a file by usig the
 *  `writeImage(...)` method. The type of file must be supported by the
 *  [ImageIO.write] method.
 *
 *  Although this class was originally designed to create an image of a
 *  component on the screen it can be used to create an image of components
 *  not displayed on a GUI. Behind the scenes the component will be given a
 *  size and the component will be layed out. The default size will be the
 *  preferred size of the component although you can invoke the `setSize()`
 *  method on the component before invoking a `createImage(...)` method. The
 *  default functionality should work in most cases. However the only
 *  foolproof way to get a image to is make sure the component has been
 *  added to a realized window with code something like the following:
 *
 *
 *      frame: JFrame = JFrame()
 *      frame.contentPane = someComponent
 *      frame.pack()
 *      ScreenImage.createImage(someComponent)
 *
 *  See: [Original Source](https://tips4java.wordpress.com/2008/10/13/screen-image/)
 */
@Suppress("MemberVisibilityCanBePrivate")
object ScreenImage {

    /**
     *  Create a [BufferedImage] for Swing components.
     *  The entire component will be captured to an image.
     *
     *  @param  component Swing component to create image from
     *  @param  bgColor The background color to be used for non-opaque components
     *  @param  scale The scale factor to be applied to the image
     *  @return a [BufferedImage], the image for the given region
     */
    fun createImage(component: JComponent, bgColor: Color? = Color.WHITE, scale: Double = 1.0): BufferedImage {
        var d = component.size
        if (d.width == 0 || d.height == 0) {
            d = component.preferredSize
            component.size = d
        }
        val region = Rectangle(0, 0, d.width, d.height)
        return createImage(component, region, bgColor, scale)
    }

    /**
     *  Create a [BufferedImage] for Swing components.
     *  All or part of the component can be captured to an image.
     *
     *  @param  component Swing component to create image from
     *  @param  region The region of the component to be captured to an image
     *  @param  bgColor The background color to be used for non-opaque components
     *  @param  scale The scale factor to be applied to the image
     *  @return a [BufferedImage], the image for the given region
     */
    fun createImage(component: JComponent, region: Rectangle, bgColor: Color? = null, scale: Double = 1.0): BufferedImage {
        val regionScaled =
            if (scale == 1.0) region
            else Rectangle(region.x, region.y, (region.width * scale).toInt(), (region.height * scale).toInt())
        //  Make sure the component has a size and has been layed out.
        //  (necessary check for components not added to a realized frame)
        if (!component.isDisplayable) {
            var d = component.size
            if (d.width == 0 || d.height == 0) {
                d = component.preferredSize
                component.size = d
            }
            layoutComponent(component)
        }
        val image = BufferedImage(regionScaled.width, regionScaled.height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        g2d.addRenderingHints(mapOf(RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_ON))
        if (scale != 1.0) g2d.transform = AffineTransform.getScaleInstance(scale, scale)

        //  Paint a background for non-opaque components,
        //  otherwise the background will be black
        if (!component.isOpaque) {
            g2d.color = bgColor ?: component.background
            g2d.fillRect(regionScaled.x, regionScaled.y, regionScaled.width, regionScaled.height)
        }
        g2d.translate(-regionScaled.x, -regionScaled.y)
        component.print(g2d)
        g2d.dispose()
        return image
    }

    private fun layoutComponent(component: Component, transparentize: Boolean = true) {
        synchronized(component.treeLock) {
            component.doLayout()
            if (component is Container) {
                if (component is JComponent && transparentize) component.isOpaque = false

                component.components.forEach { layoutComponent(it, transparentize) }
            }
        }
    }
}