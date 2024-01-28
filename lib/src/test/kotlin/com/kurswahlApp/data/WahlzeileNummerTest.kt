package com.kurswahlApp.data

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WahlzeileNummerTest {
    companion object {
        const val NUMBER = 14
    }

    @Test
    fun testSerialize() {
        val objectMapper = fachdataObjectMapper()
        val test = WahlzeileNummer(NUMBER)
        val json = objectMapper.writeValueAsString(test)
        assertEquals(json, NUMBER.toString())
        assertEquals(objectMapper.readValue<WahlzeileNummer>(json), test)
    }
}