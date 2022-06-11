package com.kurswahlApp.data

import com.kurswahlApp.getResourceURL
import com.kurswahlApp.gui.Consts
import com.kurswahlApp.readDataStruct
import java.io.File

val testFachdata = readDataStruct()

val testKurswahl = testFachdata.loadKurswahl(File(getResourceURL(Consts.TEST_FILE_NAME)!!.toURI()))