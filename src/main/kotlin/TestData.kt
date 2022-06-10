
import gui.Consts
import java.io.File

val testFachdata = readDataStruct()

val testKurswahl = testFachdata.loadKurswahl(File(getResource(Consts.TEST_FILE_NAME)!!))