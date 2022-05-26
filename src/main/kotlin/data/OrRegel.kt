package data

class OrRegel(private val regel1: RegelHolder, private val regel2: RegelHolder, desc: String? = null, errorMsg: String? = null) : Regel(desc, errorMsg) {
    override fun match(data: KurswahlData): Boolean = regel1.regel.match(data) || regel2.regel.match(data)

    override fun fillData(data: FachData) {
        regel1.regel.fillData(data)
        regel2.regel.fillData(data)
    }

    override fun toString(): String = "OrRegel(regel1=${regel1.regel}, regel2=${regel2.regel})"
}