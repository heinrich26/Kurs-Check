/*
 * Copyright (c) 2023-2025  Hendrik Horstmann
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

@file:Suppress("NOTHING_TO_INLINE")

import java.awt.geom.GeneralPath
import java.awt.geom.Point2D

fun GeneralPath.fromSVG(path: String, scale: Double = 1.0): MutableList<String> {

    val gen: Sequence<Pair<Char, List<Double>>> = sequence {
        var instruction = path[0] // wir nehmen an, dass das erste Zeichen ein Buchstabe ist
        var cur = ""
        var dot = false

        for (c in path.substring(1).replace("-", " -")) {
            if (c.isLetter()) {
                yield(
                    instruction to (cur.split(' ', ',').mapNotNull { if (it.isEmpty()) null else it.toDouble() * scale })
                )
                cur = ""
                instruction = c
                dot = false
            } else {
                if (c == '.') {
                    if (dot) {
                        cur += " 0"
                    }
                    dot = true
                } else if (dot && (c == ' ' || c == ',')) {
                    dot = false
                }
                cur += c.toString()
            }
        }
    }

    var prevInstruction = 'z'
    var handle = Point2D.Double(.0, .0)
    moveTo(.0, .0)

    val calls = mutableListOf<String>()
    fun addCall(name: String, vararg nums: Double) = calls.add(nums.joinToString(", ", "$name(", ")") { "%.4f".format(it)})

    for ((instruction, coords) in gen) {
        when (instruction) {
            'z', 'Z' -> {
                addCall("closePath")
                closePath()
            }
            'V' -> {
                addCall("lineTo", currentPoint.x, coords[0])
                lineTo(currentPoint.x, coords[0])
            }
            'v' -> {
                addCall("lineTo", currentPoint.x, currentPoint.y + coords[0])
                lineTo(currentPoint.x, currentPoint.y + coords[0])
            }
            'H' -> {
                addCall("lineTo", coords[0], currentPoint.y)
                lineTo(coords[0], currentPoint.y)
            }
            'h' -> {
                addCall("lineTo", currentPoint.x + coords[0], currentPoint.y)
                lineTo(currentPoint.x + coords[0], currentPoint.y)
            }
            'M' -> {
                addCall("moveTo", coords[0], coords[1])
                moveTo(coords[0], coords[1])
                if (coords.size > 2) {
                    for ((x, y) in coords.drop(2).chunked(2)) {
                        addCall("lineTo", x, y)
                        lineTo(x, y)
                    }
                }
            }

            'm' -> {
                addCall("moveTo", currentPoint.x + coords[0], currentPoint.y + coords[1])
                moveTo(currentPoint.x + coords[0], currentPoint.y + coords[1])
                if (coords.size > 2) {
                    for ((x, y) in coords.drop(2).chunked(2)) {
                        addCall("lineTo", currentPoint.x + x, currentPoint.y + y)
                        lineTo(currentPoint.x + x, currentPoint.y + y)
                    }
                }
            }

            'L' -> for ((x, y) in coords.chunked(2)) {
                addCall("lineTo", x, y)
                lineTo(x, y)
            }

            'l' -> for ((x, y) in coords.chunked(2)) {
                addCall("lineTo", currentPoint.x + x, currentPoint.y + y)
                lineTo(currentPoint.x + x, currentPoint.y + y)
            }

            'C' -> for ((x1, y1, x2, y2, x, y) in coords.chunked(6)) {
                addCall("curveTo", x1, y1, x2, y2, x, y)
                curveTo(x1, y1, x2, y2, x, y)
                handle = Point2D.Double(x2, y2)
            }

            'c' -> for ((dx1, dy1, dx2, dy2, dx, dy) in coords.chunked(6)) {
                currentPoint.let { (x, y) ->
                    addCall("curveTo", dx1 + x, dy1 + y, dx2 + x, dy2 + y, dx + x, dy + y)
                    curveTo(dx1 + x, dy1 + y, dx2 + x, dy2 + y, dx + x, dy + y)
                    handle = Point2D.Double(dx2 + x, dy2 + y)
                }
            }

            'Q' -> for ((x1, y1, x, y) in coords.chunked(4)) {
                addCall("quadTo", x1, y1, x, y)
                quadTo(x1, y1, x, y)
                handle = Point2D.Double(x1, y1)
            }

            'q' -> for ((dx1, dy1, dx, dy) in coords.chunked(4)) {
                currentPoint.let { (x, y) ->
                    addCall("quadTo", dx1 + x, dy1 + y, dx + x, dy + y)
                    quadTo(dx1 + x, dy1 + y, dx + x, dy + y)
                    handle = Point2D.Double(dx1 + x, dy1 + y)
                }
            }

            'S' -> for ((x1, y1, x, y) in coords.chunked(4)) {
                (if (prevInstruction in "cqs") currentPoint.mirror(handle) else currentPoint).let { (hx, hy) ->
                    addCall("curveTo", hx, hy, x1, y1, x, y)
                    curveTo(hx, hy, x1, y1, x, y)
                }
                handle = Point2D.Double(x1, y1)
            }

            's' -> for ((dx1, dy1, dx, dy) in coords.chunked(4)) {
                currentPoint.let { (x, y) ->
                    (if (prevInstruction in "cqs") currentPoint.mirror(handle) else currentPoint).let { (hx, hy) ->
                        addCall("curveTo", hx, hy, dx1 + x, dy1 + y, dx + x, dy + y)
                        curveTo(hx, hy, dx1 + x, dy1 + y, dx + x, dy + y)
                    }
                    handle = Point2D.Double(x + dx1, y + dy1)
                }
            }
            'A' -> coords.let { (rx, ry, phi, fa, fs, x2, y2) ->
                a2c(currentPoint.x, currentPoint.y, x2, y2, fa, fs, rx, ry, phi)
            }.forEach { (x1, y1, x2, y2, x, y) ->
                addCall("curveTo", x1, y1, x2, y2, x, y)
                curveTo(x1, y1, x2, y2, x, y)
            }
            'a' -> coords.let { (rx, ry, phi, fa, fs, dx, dy) ->
                currentPoint.let { (x, y) ->
                    a2c(x, y, x + dx, y + dy, fa, fs, rx, ry, phi)
                }
            }.forEach { (x1, y1, x2, y2, x, y) ->
                addCall("curveTo", x1, y1, x2, y2, x, y)
                curveTo(x1, y1, x2, y2, x, y)
            }
        }

        prevInstruction = instruction.lowercaseChar()
    }

    calls.add("trimToSize()")
    return calls
}

/**
 * Returns 6th *element* from the array.
 *
 * If the size of this array is less than 6, throws an [IndexOutOfBoundsException] except in Kotlin/JS
 * where the behavior is unspecified.
 */
private inline operator fun <T> Array<T>.component6(): T = get(5)

/**
 * Returns 6th *element* from the list.
 *
 * Throws an [IndexOutOfBoundsException] if the size of this list is less than 6.
 */
private inline operator fun <T> List<T>.component6(): T = get(5)

/**
 * Returns 7th *element* from the list.
 *
 * Throws an [IndexOutOfBoundsException] if the size of this list is less than 7.
 */
private inline operator fun <E> List<E>.component7(): E = get(6)
private inline operator fun Point2D.component1() = this.x
private inline operator fun Point2D.component2() = this.y
private fun Point2D.mirror(other: Point2D) =
    (other.clone() as Point2D).also { it.setLocation(2 * x - it.x, 2 * y - it.y) }