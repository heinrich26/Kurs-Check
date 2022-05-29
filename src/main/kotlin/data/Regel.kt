package data

import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
sealed class Regel(val desc: String?, val errorMsg: String?) {
    abstract fun match(data: KurswahlData): Boolean

    open fun fillData(data: FachData) {}
}