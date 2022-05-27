package gui

import data.Regel
import javax.swing.JLabel

class RegelLabel(regel: Regel): JLabel() {
    private val validText: String? = regel.desc
    private val invalidText: String? = regel.errorMsg
    init {
        icon = TextIcon(this, '\u2705')
        text = validText
    }

    private var apprearance = true

    fun setAppearance(valid: Boolean) {
        if (apprearance != valid) {
            if (valid) {
                text = validText
                foreground = Consts.COLOR_VALID
                (icon as TextIcon).char = Consts.CHECK_CHAR
            } else {
                text = invalidText
                foreground = Consts.COLOR_ERROR
                (icon as TextIcon).char = Consts.CROSS_CHAR
            }
            apprearance = valid
        }
    }
}