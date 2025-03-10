package com.kurswahlApp.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


class SerializationTest {
    private lateinit var fachData: FachData

    @Test
    fun testConfigLoading() {
        SchoolConfig.updateConfig()
        println(SchoolConfig.schools)
    }

    @Test
    fun testFachdataDeserialization() {
        SchoolConfig.updateConfig()
        for (id in SchoolConfig.schools.map(School::schulId)) {
            fachData = SchoolConfig.getSchool(id)!!
            println(fachData)
        }
    }

    companion object{
        @JvmStatic
        private fun collectFachData(): Array<FachData> {
            SchoolConfig.updateConfig()
            return SchoolConfig.schools.map { SchoolConfig.getSchool(it.schulId)!! }.toTypedArray()
        }
    }

    @ParameterizedTest
    @MethodSource(value = ["collectFachData"])
    fun verifyFachData(data: FachData) {
        data.checkWahlzeilen()
    }

    @Test
    fun testKurswahlDeserialization() =
        Unit

    @Test
    fun testKurswahlSerialization() = Unit
}