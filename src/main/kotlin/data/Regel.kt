package data


abstract class Regel(val desc: String?, val errorMsg: String?) {
    abstract fun match(data: KurswahlData): Boolean

    open fun fillData(data: FachData) {}
}