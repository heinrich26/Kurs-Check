package com.kurswahlApp.data

import com.kurswahlApp.data.RegelScope.*
import com.kurswahlApp.data.Wahlmoeglichkeit.DURCHGEHEND

@Suppress("unused")
class WildcardRegel(
    private val wildcard: String,
    private val anzahl: Int,
    private val wann: Wahlmoeglichkeit? = null,
    private val scope: RegelScope? = null,
    desc: String? = null,
    errorMsg: String? = null
) :
    Regel(desc, errorMsg) {

    // nicht jedes mal neu auf null checken
    private val predicate: (Wahlmoeglichkeit) -> Boolean =
        if (wann == null) { it -> (it.n >= anzahl) } else { it -> (it.n >= anzahl && wann in it) }

    private val dataScope: (KurswahlData) -> Map<Fach, Wahlmoeglichkeit> =
        when (scope) {
            null -> { it -> it.kurse }
            PF1_4 -> { it -> it.pf1_4.filterNotNull().associateWith { DURCHGEHEND } }
            PF1_5 -> { it -> it.pfs.filterNotNull().associateWith { DURCHGEHEND } }
            PF5 -> { it -> if (it.pf5 == null) emptyMap() else mapOf(it.pf5!! to DURCHGEHEND) }
            LK1_2 -> { it -> it.lks.filterNotNull().associateWith { DURCHGEHEND } }
        }

    private lateinit var wCardScope: List<Fach>

    override fun match(data: KurswahlData): Boolean {
        for ((fach, wmoegl) in dataScope.invoke(data)) {
            // Checken ob die Wahlmoeglichkeit passt
            if (!predicate(wmoegl)) continue

            // Gucken ob der Kurs passt
            if (fach in wCardScope) return true
        }

        return false
    }

    override fun fillData(data: FachData) {
        wCardScope = data.wildcards[wildcard]!!
    }

    override fun toString(): String =
        "WildcardRegel(wildcard=$wildcard, anzahl=$anzahl${if (wann != null) ", wann=$wann" else ""}${if (scope != null) ", scope=$scope" else ""}${if (desc != null) ", desc=$desc" else ""}${if (errorMsg != null) ", errorMsg=$errorMsg" else ""})"
}