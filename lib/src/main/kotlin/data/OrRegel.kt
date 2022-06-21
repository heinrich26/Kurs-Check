package com.kurswahlApp.data

@Suppress("unused")
class OrRegel(private val regel1: Regel, private val regel2: Regel, desc: String? = null, errorMsg: String? = null) : Regel(desc, errorMsg) {
    override fun match(data: KurswahlData): Boolean = regel1.match(data) || regel2.match(data)

    override fun fillData(data: FachData) {
        regel1.fillData(data)
        regel2.fillData(data)
    }

    override fun toString(): String = "OrRegel(regel1=${regel1}, regel2=${regel2})"
}