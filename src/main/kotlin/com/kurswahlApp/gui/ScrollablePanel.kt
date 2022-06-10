package com.kurswahlApp.gui

import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.LayoutManager
import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.JViewport
import javax.swing.Scrollable
import javax.swing.SwingConstants


/**
 * A panel that implements the [Scrollable] interface. This class allows you
 * to customize the scrollable features by using newly provided setter methods
 * so you don't have to extend this class every time.
 *
 * Scrollable amounts can be specifed as a percentage of the viewport size or
 * as an actual pixel value. The amount can be changed for both unit and block
 * scrolling for both horizontal and vertical scrollbars.
 *
 * The [Scrollable] interface only provides a boolean value for determining whether
 * or not the viewport size (width or height) should be used by the scrollpane
 * when determining if scrollbars should be made visible. This class supports the
 * concept of dynamically changing this value based on the size of the viewport.
 * In this case the viewport size will only be used when it is larger than the
 * panels size. This has the effect of ensuring the viewport is always full as
 * components added to the panel will be size to fill the area available,
 * based on the rules of the applicable layout manager of course.
 *
 * from: [Original Source (copied)](https://tips4java.wordpress.com/2009/12/20/scrollable-panel/)
 */
@Suppress("unused")
open class ScrollablePanel(layout: LayoutManager? = FlowLayout()) : JPanel(layout),
    Scrollable, SwingConstants {
    enum class ScrollableSizeHint {
        NONE, FIT, STRETCH
    }

    enum class IncrementType {
        PERCENT, PIXELS
    }

    private var scrollableHeight = ScrollableSizeHint.NONE
    private var scrollableWidth = ScrollableSizeHint.NONE
    private var horizontalBlock: IncrementInfo? = null
    private var horizontalUnit: IncrementInfo? = null
    private var verticalBlock: IncrementInfo? = null
    private var verticalUnit: IncrementInfo? = null

    /**
     * Get the height [ScrollableSizeHint] enum
     *
     * @return the [ScrollableSizeHint] enum for the height
     */
    fun getScrollableHeight(): ScrollableSizeHint {
        return scrollableHeight
    }

    /**
     * Set the ScrollableSizeHint enum for the height. The enum is used to
     * determine the boolean value that is returned by the
     * getScrollableTracksViewportHeight() method. The valid values are:
     *
     * - [ScrollableSizeHint.NONE] -> `false`, which causes the height
     * of the panel to be used when laying out the children
     * - [ScrollableSizeHint.FIT] -> `true`, which causes the height of
     * the viewport to be used when laying out the children
     * - [ScrollableSizeHint.STRETCH] -> `true` when the viewport height
     * is greater than the height of the panel, `false` otherwise.
     *
     * @param scrollableHeight as represented by the [ScrollableSizeHint] enum.
     */
    fun setScrollableHeight(scrollableHeight: ScrollableSizeHint) {
        this.scrollableHeight = scrollableHeight
        revalidate()
    }

    /**
     * Get the width [ScrollableSizeHint] enum
     *
     * @return the [ScrollableSizeHint] enum for the width
     */
    fun getScrollableWidth(): ScrollableSizeHint = scrollableWidth

    /**
     * Set the [ScrollableSizeHint] enum for the width. The enum is used to
     * determine the boolean value that is returned by the
     * [getScrollableTracksViewportWidth] method. The valid values are:
     *
     * - [ScrollableSizeHint.NONE] -> `false`, which causes the width
     * of the panel to be used when laying out the children
     * - [ScrollableSizeHint.FIT] -> `true`, which causes the width of
     * the viewport to be used when laying out the children
     * - [ScrollableSizeHint.STRETCH] -> `true` when the viewport width
     * is greater than the width of the panel, `false` otherwise.
     *
     * @param scrollableWidth as represented by the [ScrollableSizeHint] enum.
     */
    fun setScrollableWidth(scrollableWidth: ScrollableSizeHint) {
        this.scrollableWidth = scrollableWidth
        revalidate()
    }

    /**
     * Get the block [IncrementInfo] for the specified orientation
     *
     * @return the block [IncrementInfo] for the specified orientation
     */
    fun getScrollableBlockIncrement(orientation: Int): IncrementInfo? =
        if (orientation == SwingConstants.HORIZONTAL) horizontalBlock else verticalBlock

    /**
     * Specify the information needed to do block scrolling.
     *
     * @param orientation  specify the scrolling orientation. Must be either:
     * [SwingConstants.HORIZONTAL] or [SwingConstants.VERTICAL].
     * @param type specify how the amount parameter in the calculation of
     * the scrollable amount.Valid values are:
     * - [IncrementType.PERCENT] - treat the amount as a % of the viewport size
     * - [IncrementType.PIXELS] - treat the amount as the scrollable amount
     * @param amount a value used with the [IncrementType] to determine the
     * scrollable amount
     */
    fun setScrollableBlockIncrement(orientation: Int, type: IncrementType, amount: Int) {
        setScrollableBlockIncrement(orientation, IncrementInfo(type, amount))
    }

    /**
     * Specify the information needed to do block scrolling.
     *
     * @param orientation  specify the scrolling orientation. Must be either:
     * [SwingConstants.HORIZONTAL] or [SwingConstants.VERTICAL].
     * @param info An [IncrementInfo] object containing information on how to
     * calculate the scrollable amount.
     */
    private fun setScrollableBlockIncrement(orientation: Int, info: IncrementInfo?) {
        when (orientation) {
            SwingConstants.HORIZONTAL -> horizontalBlock = info
            SwingConstants.VERTICAL -> verticalBlock = info
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }
    }

    /**
     * Get the unit [IncrementInfo] for the specified orientation
     */
    fun getScrollableUnitIncrement(orientation: Int): IncrementInfo? =
        if (orientation == SwingConstants.HORIZONTAL) horizontalUnit else verticalUnit

    /**
     * Specify the information needed to do unit scrolling.
     *
     * @param orientation specify the scrolling orientation. Must be either:
     * [SwingConstants.HORIZONTAL] or [SwingConstants.VERTICAL].
     * @param type specify how the amount parameter in the calculation of
     * the scrollable amount. Valid values are:
     * - [IncrementType.PERCENT] - treat the amount as a % of the viewport size
     * - [IncrementType.PIXELS] - treat the amount as the scrollable amount
     * @param amount a value used with the [IncrementType] to determine the
     * scrollable amount
     */
    fun setScrollableUnitIncrement(orientation: Int, type: IncrementType, amount: Int) {
        val info = IncrementInfo(type, amount)
        setScrollableUnitIncrement(orientation, info)
    }

    /**
     * Specify the information needed to do unit scrolling.
     *
     * @param orientation specify the scrolling orientation. Must be either:
     * [SwingConstants.HORIZONTAL] or [SwingConstants.VERTICAL].
     * @param info  An [IncrementInfo] object containing information of how to
     * calculate the scrollable amount.
     */
    private fun setScrollableUnitIncrement(orientation: Int, info: IncrementInfo?) {
        when (orientation) {
            SwingConstants.HORIZONTAL -> horizontalUnit = info
            SwingConstants.VERTICAL -> verticalUnit = info
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }
    }

    //  Implement Scrollable interface
    override fun getPreferredScrollableViewportSize(): Dimension = preferredSize

    override fun getScrollableUnitIncrement(
        visible: Rectangle, orientation: Int, direction: Int
    ): Int {
        return when (orientation) {
            SwingConstants.HORIZONTAL -> getScrollableIncrement(horizontalUnit, visible.width)
            SwingConstants.VERTICAL -> getScrollableIncrement(verticalUnit, visible.height)
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }
    }

    override fun getScrollableBlockIncrement(
        visible: Rectangle, orientation: Int, direction: Int
    ): Int = when (orientation) {
        SwingConstants.HORIZONTAL -> getScrollableIncrement(horizontalBlock, visible.width)
        SwingConstants.VERTICAL -> getScrollableIncrement(verticalBlock, visible.height)
        else -> throw IllegalArgumentException("Invalid orientation: $orientation")
    }

    private fun getScrollableIncrement(info: IncrementInfo?, distance: Int): Int =
        if (info!!.increment == IncrementType.PIXELS) info.amount else distance * info.amount / 100

    override fun getScrollableTracksViewportWidth(): Boolean {
        if (scrollableWidth == ScrollableSizeHint.NONE) return false
        if (scrollableWidth == ScrollableSizeHint.FIT) return true

        //  STRETCH sizing, use the greater of the panel or viewport width
        return if (parent is JViewport) {
            (parent as JViewport).width > preferredSize.width
        } else false
    }

    override fun getScrollableTracksViewportHeight(): Boolean {
        if (scrollableHeight == ScrollableSizeHint.NONE) return false
        if (scrollableHeight == ScrollableSizeHint.FIT) return true

        //  STRETCH sizing, use the greater of the panel or viewport height
        return if (parent is JViewport) {
            (parent as JViewport).height > preferredSize.height
        } else false
    }

    /**
     * Helper class to hold the information required to calculate the scroll amount.
     */
    class IncrementInfo(val increment: IncrementType, val amount: Int) {

        override fun toString(): String = "ScrollablePanel[$increment, $amount]"
    }

    init {
        val block = IncrementInfo(IncrementType.PERCENT, 100)
        val unit = IncrementInfo(IncrementType.PERCENT, 10)
        setScrollableBlockIncrement(SwingConstants.HORIZONTAL, block)
        setScrollableBlockIncrement(SwingConstants.VERTICAL, block)
        setScrollableUnitIncrement(SwingConstants.HORIZONTAL, unit)
        setScrollableUnitIncrement(SwingConstants.VERTICAL, unit)
    }
}