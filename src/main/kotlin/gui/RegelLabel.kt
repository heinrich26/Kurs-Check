package gui

import createImageIcon
import data.Regel
import wrappable
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

class RegelLabel(regel: Regel) : JLabel(regel.desc!!.wrappable(), validIcon, TRAILING) {
    private val validText: String = regel.desc!!.wrappable()
    private val invalidText: String = regel.errorMsg!!.wrappable()


    init {
//        icon = TextIcon(this, '\u2705')
        border = EmptyBorder(0, 4, 0, 0)
        text = validText
        foreground = Consts.COLOR_VALID
    }

    private var apprearance = true

    fun setAppearance(valid: Boolean) {
        if (apprearance != valid) {
            if (valid) {
                icon = validIcon
                text = validText
                foreground = Consts.COLOR_VALID
//                (icon as TextIcon).char = Consts.CHECK_CHAR
            } else {
                text = invalidText
                foreground = Consts.COLOR_ERROR
//                (icon as TextIcon).char = Consts.CROSS_CHAR
                icon = errorIcon
            }
            apprearance = valid
        }
    }

    companion object {
        private val validIcon = createImageIcon("check.png")
        private val errorIcon = createImageIcon("cross.png")
    }
}