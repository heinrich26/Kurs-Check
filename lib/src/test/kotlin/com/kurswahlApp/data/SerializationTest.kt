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

    @Test()
    fun testKurswahlDeserialization() {
        val json =  """
{"jsonVersion":"1.0","lk1":"Ma","lk2":"VWL/BWL","pf3":"En","pf4":"Ge","pf5":"DS","pf5Typ":"praes","gks":{"De":"1-4","RW":"1-2","Ch":"1-4","Sp":"1-4"},"fremdsprachen":{"En":1,"Fr":7},"wpfs":{"first":"--","second":null},"klasse":null,"wahlzeile":6,"vorname":"dsfs","nachname":"sdfdsdfsasffsyys","geburtsdatum":"2010-01-07","geburtsort":"fsyysf","staatsangehoerigkeit":"DE","schulId":"osz_banken.json","umfrageData": []}
        """
        SchoolConfig.updateConfig()
        val school = SchoolConfig.getSchool("osz_banken.json")!!
        val data = school.loadKurswahl(json)
        println(data)
    }

    @Test
    fun testKurswahlSerialization() = Unit
}