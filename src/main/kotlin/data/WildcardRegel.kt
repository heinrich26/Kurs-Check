package data

class WildcardRegel(
    private val wildcard: String,
    private val anzahl: Int,
    private val wann: Wahlmoeglichkeit? = null,
    desc: String,
    errorMsg: String
) :
    Regel(desc, errorMsg) {
    // nicht jedes mal neu auf null checken
    private val predicate: (Wahlmoeglichkeit) -> Boolean =
        if (wann == null) { it -> (it.n >= anzahl) } else { it -> (it.n >= anzahl && it == wann) }

    private var scope: List<Fach> = emptyList()

    override fun match(data: KurswahlData): Boolean {
//        val scope = fachData.wildcards[wildcard]!!
        for ((fach, wmoegl) in data.kurse) {
            // Checken ob die Wahlmoeglichkeit passt
            if (!predicate(wmoegl)) continue

            // Gucken ob der Kurs passt
            if (fach in scope) return true
        }

        // TODO Alternative Implementierung vergleichen
//        val kurse = data.kurse
//        scope.mapNotNull { data.kurse[it] }.forEach { if (predicate(it)) return true }

        return false
    }

    override fun fillData(data: FachData) {
        scope = data.wildcards[wildcard]!!
    }
}