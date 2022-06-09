package data

class KuerzelRegel(
    private val kuerzel: String,
    private val anzahl: Int,
    private val wann: Wahlmoeglichkeit? = null,
    private val scope: RegelScope? = null,
    desc: String? = null,
    errorMsg: String? = null
) : Regel(desc, errorMsg) {

    private val predicate: (Wahlmoeglichkeit) -> Boolean =
        if (wann == null) { it -> (it.n >= anzahl) } else { it -> (it.n >= anzahl && wann in it) }

    private val dataScope: (KurswahlData) -> Map<Fach, Wahlmoeglichkeit> =
        when (scope) {
            null -> { it -> it.kurse }
            RegelScope.PF1_4 -> { it -> it.pf1_4.filterNotNull().associateWith { Wahlmoeglichkeit.DURCHGEHEND } }
            RegelScope.PF1_5 -> { it -> it.pfs.filterNotNull().associateWith { Wahlmoeglichkeit.DURCHGEHEND } }
            RegelScope.PF5 -> { it -> if (it.pf5 == null) emptyMap() else mapOf(it.pf5!! to Wahlmoeglichkeit.DURCHGEHEND) }
            RegelScope.LK1_2 -> { it -> it.lks.filterNotNull().associateWith { Wahlmoeglichkeit.DURCHGEHEND } }
        }

    private lateinit var target: Fach

    override fun match(data: KurswahlData): Boolean {
        for ((fach, wmoegl) in dataScope(data)) {
            // Gucken ob das Kürzel passt
            if (fach != target) continue
            // Checken ob die Wahlmoeglichkeit passt
            if (predicate(wmoegl)) return true

        }

        return false
    }

    override fun fillData(data: FachData) {
        target = data.faecher.find { it.kuerzel == kuerzel }!!
    }

    override fun toString(): String =
        "KürzelRegel(kürzel=$kuerzel, anzahl=$anzahl${if (wann != null) ", wann=$wann" else ""}${if (scope != null) ", scope=$scope" else ""}${if (desc != null) ", desc=$desc" else ""}${if (errorMsg != null) ", errorMsg=$errorMsg" else ""})"

    override fun hashCode(): Int {
        var result = kuerzel.hashCode()
        result = 31 * result + anzahl.hashCode()
        result = 31 * result + wann.hashCode()
        return 31 * result + scope.hashCode()
    }
}