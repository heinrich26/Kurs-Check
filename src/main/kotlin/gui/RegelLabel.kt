package gui

import data.Regel
import javax.swing.JLabel

class RegelLabel(regel: Regel): JLabel() {
    private val validText: String = "<html><div style=\"width:160px;\">${regel.desc}</div></html>"
    private val invalidText: String = "<html>${regel.errorMsg}</html>"
    init {
        icon = TextIcon(this, '\u2705')
        text = validText
    }

    private var apprearance = true

    fun setAppearance(valid: Boolean) {
        if (apprearance != valid) {
            if (valid) {
                (icon as TextIcon).char = Consts.CHECK_CHAR
                text = validText
                foreground = Consts.COLOR_VALID
            } else {
                text = invalidText
                foreground = Consts.COLOR_ERROR
                (icon as TextIcon).char = Consts.CROSS_CHAR
            }
            apprearance = valid
        }
    }
}