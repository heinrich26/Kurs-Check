package data

class IfThenRegel(private val regel1: RegelHolder, private val regel2: RegelHolder, desc: String, errorMsg: String): Regel(desc, errorMsg) {

    override fun match(data: KurswahlData): Boolean {
        return if (regel1.regel.match(data)) regel2.regel.match(data) else true
    }

    override fun fillData(data: FachData) {
        regel1.regel.fillData(data)
        regel2.regel.fillData(data)
    }

    override fun toString(): String = "IfThenRegel(regel1=${regel1.regel}, regel2=${regel2.regel})"
}