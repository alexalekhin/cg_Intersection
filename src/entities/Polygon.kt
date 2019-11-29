package entities

import kotlin.math.PI
import kotlin.math.abs

data class Polygon(val leftEdges: MutableList<HalfPlain>, val rightEdges: MutableList<HalfPlain>) {

    fun findTop() = when { // FIXME
        (leftEdges.isNotEmpty() && rightEdges.isNotEmpty()) -> if (abs(leftEdges[0].getPolarAngle()!!) in (PI / 2)..PI || abs(rightEdges[0].getPolarAngle()!!) in (PI / 2)..PI) {
            leftEdges[0].isIntersectedBy(rightEdges[0]) ?: Point(Double.NaN, Double.POSITIVE_INFINITY) // FIXME
        } else {
            Point(Double.NaN, Double.POSITIVE_INFINITY)
        }
        (leftEdges.isNotEmpty() && rightEdges.isEmpty()) -> when {
            abs(leftEdges[0].getPolarAngle()!!) < PI && abs(leftEdges[0].getPolarAngle()!!) > PI / 2 ->
                Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            abs(leftEdges[0].getPolarAngle()!!) < PI / 2 && abs(leftEdges[0].getPolarAngle()!!) > 0.0 ->
                Point(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            abs(leftEdges[0].getPolarAngle()!!) == PI -> Point(Double.NaN, -(leftEdges[0].line.c) / leftEdges[0].line.b)
            else -> Point(-(leftEdges[0].line.c) / leftEdges[0].line.a, Double.POSITIVE_INFINITY)
        }
        (leftEdges.isEmpty() && rightEdges.isNotEmpty()) -> when {
            abs(rightEdges[0].getPolarAngle()!!) < PI && abs(rightEdges[0].getPolarAngle()!!) > PI / 2 ->
                Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            abs(rightEdges[0].getPolarAngle()!!) < PI / 2 && abs(rightEdges[0].getPolarAngle()!!) > 0.0 ->
                Point(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            abs(rightEdges[0].getPolarAngle()!!) == 0.0 -> Point(Double.NaN, -(rightEdges[0].line.c) / rightEdges[0].line.b)
            else -> Point(-(rightEdges[0].line.c) / rightEdges[0].line.a, Double.POSITIVE_INFINITY)
        }
        else -> Point(Double.NaN, Double.NaN)
    }

    fun findBottom() = when { // FIXME
        (leftEdges.isNotEmpty() && rightEdges.isNotEmpty()) -> if (abs(leftEdges.last().getPolarAngle()!!) in 0.0..(PI / 2) || abs(rightEdges.last().getPolarAngle()!!) in 0.0..(PI / 2)) {
            leftEdges.last().isIntersectedBy(rightEdges.last()) ?: Point(leftEdges.last().line.c, Double.NEGATIVE_INFINITY)
        } else {
            Point(Double.NaN, Double.NEGATIVE_INFINITY)
        }
        (leftEdges.isNotEmpty() && rightEdges.isEmpty()) -> when {
            abs(leftEdges.last().getPolarAngle()!!) < PI / 2 && abs(leftEdges.last().getPolarAngle()!!) > 0.0 ->
                Point(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
            abs(leftEdges.last().getPolarAngle()!!) < PI && abs(leftEdges.last().getPolarAngle()!!) > PI / 2 ->
                Point(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
            abs(leftEdges.last().getPolarAngle()!!) == PI -> Point(Double.NaN, -(leftEdges.last().line.c) / leftEdges.last().line.b)
            else -> Point(-(leftEdges.last().line.c) / leftEdges.last().line.a, Double.POSITIVE_INFINITY)
        }
        (leftEdges.isEmpty() && rightEdges.isNotEmpty()) -> when {
            abs(rightEdges.last().getPolarAngle()!!) < PI / 2 && abs(rightEdges.last().getPolarAngle()!!) > 0.0 ->
                Point(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
            abs(rightEdges.last().getPolarAngle()!!) < PI && abs(rightEdges.last().getPolarAngle()!!) > PI / 2 ->
                Point(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
            abs(rightEdges.last().getPolarAngle()!!) == 0.0 -> Point(Double.NaN, -(rightEdges.last().line.c) / rightEdges.last().line.b)
            else -> Point(-(rightEdges.last().line.c) / rightEdges.last().line.a, Double.POSITIVE_INFINITY)
        }
        else -> Point(Double.NaN, Double.NaN)
    }

    fun findTopCornerOfLeftEdge(halfPlain: HalfPlain, halfPlainIndex: Int): Point {
        return if (leftEdges[0] == halfPlain) findTop() else halfPlain.isIntersectedBy(leftEdges[halfPlainIndex - 1])!!
    }

    fun findTopCornerOfRightEdge(halfPlain: HalfPlain, halfPlainIndex: Int): Point {
        return if (rightEdges[0] == halfPlain) findTop() else halfPlain.isIntersectedBy(rightEdges[halfPlainIndex - 1])!!
    }

    fun findBottomCornerOfLeftEdge(halfPlain: HalfPlain, halfPlainIndex: Int): Point {
        return if (leftEdges.last() == halfPlain) findBottom() else halfPlain.isIntersectedBy(leftEdges[halfPlainIndex + 1])!!
    }

    fun findBottomCornerOfRightEdge(halfPlain: HalfPlain, halfPlainIndex: Int): Point {
        return if (rightEdges.last() == halfPlain) findBottom() else halfPlain.isIntersectedBy(rightEdges[halfPlainIndex + 1])!!
    }
}