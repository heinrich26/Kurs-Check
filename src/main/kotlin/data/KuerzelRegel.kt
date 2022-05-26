package data

class KuerzelRegel(
    private val kuerzel: String,
    private val anzahl: Int,
    private val wann: Wahlmoeglichkeit? = null,
    desc: String? = null,
    errorMsg: String? = null
) : Regel(desc, errorMsg) {

    private val predicate: (Wahlmoeglichkeit) -> Boolean =
        if (wann == null) { it -> (it.n >= anzahl) } else { it -> (it.n >= anzahl && it in wann) }

    private var target: Fach? = null

    override fun match(data: KurswahlData): Boolean {
        for ((fach, wmoegl) in data.kurse) {
            // Gucken ob das Kürzel passt
            if (fach != target) continue
            // Checken ob die Wahlmoeglichkeit passt
            if (predicate(wmoegl)) return true

        }

        return false
    }

    override fun fillData(data: FachData) {
        target = data.faecher.find { it.kuerzel == kuerzel }
    }
}