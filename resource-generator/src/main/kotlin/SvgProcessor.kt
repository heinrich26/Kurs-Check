/*
 * Copyright (c) 2023-2024  Hendrik Horstmann
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

import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.PropertyKey
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.io.File
import java.text.MessageFormat
import java.util.*
import javax.annotation.processing.Filer
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


fun process(resourcesDir: String, dest: Any) {
    val resources = File(resourcesDir).listFiles { _, name -> name.endsWith(".svg") }
    // Generate the Kotlin file for the resources
    val className = "R"
    val file = FileSpec.builder("com.kurswahlApp", className).addType(
        TypeSpec.objectBuilder(className).apply {
            resources?.forEach { resourceFile ->
                getPathData(resourceFile)?.let { (data, sx, _) ->
                    val propertyName = resourceFile.name.removeSuffix(".svg")
                    addProperty(
                        PropertySpec.builder(propertyName, Path2D::class)
                            .initializer(
                                "%T().apply {\n" +
                                        "\t${
                                            GeneralPath().fromSVG(data, sx /* nur einen Faktor nutzen, ist einfacher */)
                                                .joinToString("\n\t")
                                        }\n" +
                                        "}", GeneralPath::class
                            )
                            .build()
                    )
                }

            }
            addProperty(
                PropertySpec.builder("bundle", ResourceBundle::class, KModifier.PRIVATE)
                    .initializer("ResourceBundle.getBundle(%S)", "kursCheckStrings")
                    .build()
            )
            addFunction(
                FunSpec.builder("getString")
                    .returns(String::class)
                    .addParameter(
                        ParameterSpec.builder("key", String::class).addAnnotation(
                            AnnotationSpec.builder(PropertyKey::class)
                                .addMember("resourceBundle = %S", "kursCheckStrings")
                                .build()
                        ).build()
                    )
                    .addParameter("params", Any::class.asTypeName().copy(nullable = true), KModifier.VARARG)
                    .addStatement("val value: String = bundle.getString(key)")
                    .addStatement("return if (params.isNotEmpty()) %T.format(value, *params) else value", MessageFormat::class)
                    .build()
            )
        }.build()
    ).build()

    if (dest is File) {
        dest.mkdirs()
        file.writeTo(dest)
    } else if (dest is Filer) {
        file.writeTo(dest)
    }
}

fun main(args: Array<String>) {
    process(args[0] + "/drawables", File(args[2]))
}

fun getPathData(f: File): Triple<String, Double, Double>? {
    val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()

    // optional, but recommended
    // process XML securely, avoid attacks like XML External Entities (XXE)
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)

    // parse XML file
    val db: DocumentBuilder = dbf.newDocumentBuilder()
    val doc: Document = db.parse(f)

    // optional, but recommended
    // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    doc.documentElement.normalize()

    val w = doc.documentElement.getAttribute("width").removeSuffix("px").toInt()
    val h = doc.documentElement.getAttribute("height").removeSuffix("px").toInt()
    val bounds = doc.documentElement.getAttribute("viewBox").split(' ').map(String::toDouble)
    val sx = w / bounds[2]
    val sy = h / bounds[3]

    // get the paths
    var out = ""
    val list: NodeList = doc.getElementsByTagName("path")
    for (i in 0 until list.length) {
        val node: Node = list.item(i)
        if (node.nodeType == Node.ELEMENT_NODE) {
            node as Element
            if (!node.hasAttribute("fill") || node.getAttribute("fill") != "none")
                out += " " + node.getAttribute("d")

        }
    }
    return out.ifEmpty { null }?.let { Triple(out, sx, sy) }
}