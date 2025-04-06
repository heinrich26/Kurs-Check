/*
 * Copyright (c) 2023-2025  Hendrik Horstmann
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

package com.kurswahlApp.data.lusd_pdf

import com.kurswahlApp.data.Wahlmoeglichkeit
import com.kurswahlApp.data.checked
import org.apache.pdfbox.pdmodel.interactive.form.PDField

class FeldZeile {

    var lk1: PDField? = null
    var lk2: PDField? = null
    var lk3: PDField? = null
    var pf3: PDField? = null
    var pf4: PDField? = null
    var pk5: PDField? = null
    var q1: PDField? = null
    var q2: PDField? = null
    var q3: PDField? = null
    var q4: PDField? = null

    var wahlmoeglichkeit: Wahlmoeglichkeit?
        get() = Wahlmoeglichkeit.fromBools(listOf(q1?.checked == true, q2?.checked == true, q3?.checked == true, q4?.checked == true))
        set(value) {
            val bools = value?.bools ?: Wahlmoeglichkeit.UNGEWAEHLT_BOOLS
            q1?.checked = bools[0]
            q2?.checked = bools[1]
            q3?.checked = bools[2]
            q4?.checked = bools[3]
        }


    fun checkLK1() {
        lk1?.checked = true
    }

    fun checkLK2() {
        lk2?.checked = true
    }

    fun checkLK3() {
        lk3?.checked = true
    }

    fun checkPF3() {
        pf3?.checked = true
    }

    fun checkPF4() {
        pf4?.checked = true
    }

    fun checkPK5() {
        pk5?.checked = true
    }

    fun clear() {
        for (field in listOf(lk1, lk2, lk3, pf3, pf4, pk5, q1, q2, q3, q4)) {
            field?.checked = false
        }
    }

    companion object {
//        @JvmStatic
//        fun fromList(fields: List<PDField>) = FeldZeile(
//                fields[0],
//                fields[1],
//                fields[2],
//                fields[3],
//                fields[4],
//                fields[5],
//                fields[6],
//                fields[7],
//                fields[8],
//                fields[9]
//            )
    }
}
