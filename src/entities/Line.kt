package entities

import kotlin.math.*

data class Line(val a: Double, val b: Double, val c: Double) {
    /* If intersected returns point else null */
    fun isIntersectedBy(line: Line): Point? {
        return if ((a / b) / (line.a / line.b) == 1.0) {
            null
        } else {
            val x = (line.c - c * line.b) / (a * line.b - b * line.a)
            Point(x, -(c + a * x) / b)
        }
    }

    /* Finds polar angle between (0,1) and (a, b) */
    fun getPolarAngle(): Double? {
        // TODO: use epsilon
        val isLeft = (a > 0 && b >= Double.NEGATIVE_INFINITY && b < Double.POSITIVE_INFINITY) || (a == 0.0 && b < 0)
        val isRight = (a < 0 && b > Double.NEGATIVE_INFINITY && b <= Double.POSITIVE_INFINITY) || (a == 0.0 && b > 0)
        return when {
            isLeft -> acos(b / sqrt(a.pow(2) + b.pow(2)))
            isRight -> -acos(b / sqrt(a.pow(2) + b.pow(2)))
            else -> null // Not related to problem definition
        }
    }
    // TODO: use Epsilon
    fun isPointInside(point: Point) = (a * point.x + b * point.y == -c)

    companion object {
        @JvmStatic
        fun fromString(lineString: String) = try {
            val coefficients = lineString.split(" ").map { it.toDouble() }
            Line(coefficients[0], coefficients[1], coefficients[2])
        } catch (e: NumberFormatException) {
//            Log.debug("Error in Line parsing")
            null
        }
    }
}