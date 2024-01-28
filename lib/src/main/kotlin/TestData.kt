/*
 * Copyright (c) 2022-2024  Hendrik Horstmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kurswahlApp.data

val testFachdata: FachData
    get() = SchoolConfig.getSchool("paulsen.json")!!

const val testJsonString = """{"jsonVersion":"1.3","lk1":"ma","lk2":"in","pf3":"e1","pf4":"ek","pf5":"ph","pf5_typ":"praes","gks":{"de":"1-4","ku":"1-3","ge":"3-4","ch":"1-3","sp":"1-4","be":"1-2","tl":"1-2"},"fremdsprachen":{"e1":3,"f2":7},"wpfs":{"first":"in","second":"BioCh"},"klasse":null,"wahlzeile":29,"vorname":"Hendrik Sven","nachname":"Horstmann","geburtsdatum":"2005-09-27","geburtsort":"Berlin","staatsangehoerigkeit":"DE","schulId":"paulsen.json"}"""

val testKurswahl: KurswahlData
    get() = testFachdata.loadKurswahl(testJsonString)