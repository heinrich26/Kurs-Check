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

import kotlin.math.*

/**
 * Converts an arc to a sequence of cubic bézier curves
 */


private var TAU = Math.PI * 2


/**
 * Calculate an angle between two unit vectors
 *
 * Since we measure angle between radii of circular arcs,
 * we can use simplified math (without length normalization)
 */
private fun unitVectorAngle(ux: Double, uy: Double, vx: Double, vy: Double): Double {
    val sign = if (ux * vy - uy * vx < 0) -1 else 1
    val dot  = (ux * vx + uy * vy).coerceIn(-1.0, 1.0) // rounding errors, e.g. -1.0000000000000002 can screw up this

    // Add this to work with arbitrary vectors:
    // dot /= Math.sqrt(ux * ux + uy * uy) * Math.sqrt(vx * vx + vy * vy);

    return sign * acos(dot)
}

/**
 * Convert from endpoint to center parameterization,
 * see http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes
 *
 * Return [cx, cy, theta1, delta_theta]
 */
private fun getArcCenter(x1: Double, y1: Double, x2: Double, y2: Double, fa: Double, fs: Double, rx: Double, ry: Double, sinPhi: Double, cosPhi: Double): Array<Double> {
    // Step 1.
    //
    // Moving an ellipse so origin will be the middlepoint between our two
    // points. After that, rotate it to line up ellipse axes with coordinate
    // axes.
    //
    val x1p =  cosPhi*(x1-x2)/2 + sinPhi*(y1-y2)/2
    val y1p = -sinPhi*(x1-x2)/2 + cosPhi*(y1-y2)/2

    val rxSq  =  rx * rx
    val rySq  =  ry * ry
    val x1pSq = x1p * x1p
    val y1pSq = y1p * y1p

    // Step 2.
    //
    // Compute coordinates of the centre of this ellipse (cx', cy')
    // in the new coordinate system.
    //
    var radicant = (rxSq * rySq) - (rxSq * y1pSq) - (rySq * x1pSq)

    if (radicant < 0) {
        // due to rounding errors it might be e.g. -1.3877787807814457e-17
        radicant = 0.0
    }

    radicant /=   (rxSq * y1pSq) + (rySq * x1pSq)
    radicant = sqrt(radicant) * (if (fa == fs) -1 else 1)

    val cxp = radicant *  rx/ry * y1p
    val cyp = radicant * -ry/rx * x1p

    // Step 3.
    //
    // Transform back to get centre coordinates (cx, cy) in the original
    // coordinate system.
    //
    val cx = cosPhi*cxp - sinPhi*cyp + (x1+x2)/2
    val cy = sinPhi*cxp + cosPhi*cyp + (y1+y2)/2

    // Step 4.
    //
    // Compute angles (theta1, delta_theta).
    //
    val v1x =  (x1p - cxp) / rx
    val v1y =  (y1p - cyp) / ry
    val v2x = (-x1p - cxp) / rx
    val v2y = (-y1p - cyp) / ry

    val theta1 = unitVectorAngle(1.0, 0.0, v1x, v1y)
    var deltaTheta = unitVectorAngle(v1x, v1y, v2x, v2y)

    if (fs == 0.0 && deltaTheta > 0) {
        deltaTheta -= TAU
    }
    if (fs == 1.0 && deltaTheta < 0) {
        deltaTheta += TAU
    }

    return arrayOf(cx, cy, theta1, deltaTheta)
}

/**
 * Approximate one unit arc segment with bézier curves,
 * see http://math.stackexchange.com/questions/873224
 */
private fun approximateUnitArc(theta1: Double, deltaTheta: Double): Array<Double> {
    val alpha = 4/3 * tan(deltaTheta/4)

    val x1 = cos(theta1)
    val y1 = sin(theta1)
    val x2 = cos(theta1 + deltaTheta)
    val y2 = sin(theta1 + deltaTheta)

    return arrayOf(x1, y1, x1 - y1*alpha, y1 + x1*alpha, x2 + y2*alpha, y2 - x2*alpha, x2, y2)
}


/**
 * Converts an arc to a sequence of cubic bézier curves
 */
fun a2c(x1: Double, y1: Double, x2: Double, y2: Double, fa: Double, fs: Double, rx: Double, ry: Double, phi: Double): List<Array<Double>> {
    val sinPhi = sin(phi * TAU / 360)
    val cosPhi = cos(phi * TAU / 360)

    // Make sure radii are valid
    //
    val x1p =  cosPhi*(x1-x2)/2 + sinPhi*(y1-y2)/2
    val y1p = -sinPhi*(x1-x2)/2 + cosPhi*(y1-y2)/2

    if (x1p == 0.0 && y1p == 0.0) {
        // we're asked to draw line to itself
        return emptyList()
    }

    if (rx == 0.0 || ry == 0.0) {
        // one of the radii is zero
        return emptyList()
    }


    // Compensate out-of-range radii
    //
    var rx2 = abs(rx)
    var ry2 = abs(ry)

    val lambda = (x1p * x1p) / (rx2 * rx2) + (y1p * y1p) / (ry2 * ry2)
    if (lambda > 1) {
        rx2 *= sqrt(lambda)
        ry2 *= sqrt(lambda)
    }


    // Get center parameters (cx, cy, theta1, delta_theta)
    //
    val cc = getArcCenter(x1, y1, x2, y2, fa, fs, rx2, ry2, sinPhi, cosPhi)

    var theta1 = cc[2]
    var deltaTheta = cc[3]

    // Split an arc to multiple segments, so each segment
    // will be less than τ/4 (= 90°)
    //
    val segments = ceil(abs(deltaTheta) / (TAU / 4)).coerceAtLeast(1.0).roundToInt()
    val result = arrayOfNulls<Array<Double>>(segments)
    deltaTheta /= segments

    for (i in 0 until segments) {
        result[i] = approximateUnitArc(theta1, deltaTheta)
        theta1 += deltaTheta
    }

    // We have a bezier approximation of a unit circle,
    // now need to transform back to the original ellipse
    //
    return result.map { curve ->
        for (i in 0 until curve!!.size step 2) {
            var x = curve[i + 0]
            var y = curve[i + 1]

            // scale
            x *= rx2
            y *= ry2

            // rotate
            val xp = cosPhi*x - sinPhi*y
            val yp = sinPhi*x + cosPhi*y

            // translate
            curve[i + 0] = xp + cc[0]
            curve[i + 1] = yp + cc[1]
        }

        return@map curve
    }
}