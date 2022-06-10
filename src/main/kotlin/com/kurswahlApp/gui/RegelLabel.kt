package com.kurswahlApp.gui

import com.kurswahlApp.data.Regel
import com.kurswahlApp.createImageIcon
import com.kurswahlApp.wrappable
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

class RegelLabel(regel: Regel) : JLabel(regel.desc!!.wrappable(), validIcon, LEADING) {
    private val validText: String = regel.desc!!.wrappable()
    private val invalidText: String = regel.errorMsg!!.wrappable()


    init {
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
            } else {
                text = invalidText
                foreground = Consts.COLOR_ERROR
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