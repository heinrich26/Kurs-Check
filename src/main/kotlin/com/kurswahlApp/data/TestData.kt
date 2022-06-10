package com.kurswahlApp.data

import com.kurswahlApp.gui.Consts
import com.kurswahlApp.getResource
import com.kurswahlApp.readDataStruct
import java.io.File

val testFachdata = readDataStruct()

val testKurswahl = testFachdata.loadKurswahl(File(getResource(Consts.TEST_FILE_NAME)!!))