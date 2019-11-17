package entities

data class Polygon(val leftEdges: MutableList<HalfPlain>, val rightEdges: MutableList<HalfPlain>) {
    fun findTop() = when {
        (leftEdges.isNotEmpty() && rightEdges.isNotEmpty()) ->
            leftEdges[0].isIntersectedBy(rightEdges[0]) ?: Point(0.0, Double.POSITIVE_INFINITY) // FIXME
        (leftEdges.isNotEmpty() && rightEdges.isEmpty()) -> Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        (leftEdges.isEmpty() && rightEdges.isNotEmpty()) -> Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        else -> Point(0.0, Double.POSITIVE_INFINITY)
    }

    fun findBottom() = when {
        (leftEdges.isNotEmpty() && rightEdges.isNotEmpty()) ->
            leftEdges.last().isIntersectedBy(rightEdges.last()) ?: Point(leftEdges.last().line.c, Double.NEGATIVE_INFINITY)
        (leftEdges.isNotEmpty() && rightEdges.isEmpty()) -> Point(leftEdges.last().line.c, Double.NEGATIVE_INFINITY)
        (leftEdges.isEmpty() && rightEdges.isNotEmpty()) -> Point(rightEdges.last().line.c, Double.NEGATIVE_INFINITY)
        else -> Point(0.0, Double.NEGATIVE_INFINITY)
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