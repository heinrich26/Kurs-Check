package data


class IfThenRegel(private val regel1: Regel, private val regel2: Regel, desc: String, errorMsg: String) :
    Regel(desc, errorMsg) {

    override fun match(data: KurswahlData): Boolean = if (regel1.match(data)) regel2.match(data) else true

    override fun fillData(data: FachData) {
        regel1.fillData(data)
        regel2.fillData(data)
    }

    override fun toString(): String = "IfThenRegel(regel1=${regel1}, regel2=${regel2})"

    override fun hashCode(): Int = 31 * regel1.hashCode() + regel2.hashCode()
}