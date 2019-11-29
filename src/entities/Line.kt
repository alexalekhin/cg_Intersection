package entities

import EPSILON
import kotlin.math.*

data class Line(val a: Double, val b: Double, val c: Double) {

    fun isIntersectedBy(line: Line): Point? {
        return when {
            abs(b) <= EPSILON && abs(line.b) <= EPSILON -> null
            (abs((a / b) / (line.a / line.b) - 1.0) <= EPSILON) -> null
            else -> {
                val y = (line.a * c - a * line.c) / (a * line.b - b * line.a)
                Point((-c - b * y) / a, y)
            }
        }
    }

    /* Finds polar angle between (0,1) and (a, b) */
    fun getPolarAngle(): Double? {
        // TODO: use epsilon
        val isLeft = (a > 0 && b >= Double.NEGATIVE_INFINITY && b < Double.POSITIVE_INFINITY) || (a == 0.0 && b < 0.0)
        val isRight = (a < 0 && b > Double.NEGATIVE_INFINITY && b <= Double.POSITIVE_INFINITY) || (a == 0.0 && b > 0.0)
        return when {
            isLeft -> acos(b / sqrt(a.pow(2) + b.pow(2)))
            isRight -> -acos(b / sqrt(a.pow(2) + b.pow(2)))
            else -> null // Not related to problem definition
        }
    }

    // TODO: use Epsilon
    fun isPointInside(point: Point) = (abs(a * point.x + b * point.y + c) <= EPSILON)

    companion object {
        @JvmStatic
        fun fromString(lineString: String) = try {
            val coefficients = lineString.split(" ").map { it.toDouble() }
            Line(coefficients[0], coefficients[1], coefficients[2])
        } catch (e: NumberFormatException) {
            null
        }
    }
}