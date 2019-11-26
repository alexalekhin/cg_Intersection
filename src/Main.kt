import entities.HalfPlain
import entities.Line
import entities.Point
import entities.Polygon
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

//Common
const val EPSILON = 0.000001
// Left
const val TOP_LEFT_ANGLE = 0.0
const val BOTTOM_LEFT_ANGLE = PI - EPSILON
// Right
const val TOP_RIGHT_ANGLE = TOP_LEFT_ANGLE - EPSILON
const val BOTTOM_RIGHT_ANGLE = -PI
val LEFT_RANGE = TOP_LEFT_ANGLE..BOTTOM_LEFT_ANGLE
val RIGHT_RANGE = BOTTOM_RIGHT_ANGLE..TOP_RIGHT_ANGLE

fun checkIsLeft(angle: Double) = (angle in LEFT_RANGE)

fun checkIsRight(angle: Double) = (angle in RIGHT_RANGE)

fun main(args: Array<String>) {
    val res = findIntersection(parseHalfPlainsFromFile(File(args[0])).toMutableList())
    print("")
}

fun parseHalfPlainsFromFile(file: File): List<HalfPlain> {
    val lines = file.readLines()
    val linesNum = lines[0].toInt()
    return parseHalfPlainsFromListOfStrings(lines.subList(1, lines.size))
}

fun parseHalfPlainsFromListOfStrings(listOfStrings: List<String>): List<HalfPlain> {
    val list = mutableListOf<HalfPlain>()
    listOfStrings.forEach { list.add(HalfPlain.fromString(it)!!) }
    return list
}

fun findIntersection(list: MutableList<HalfPlain>): Polygon? = when (list.size) {     // TODO: sort counter clock-wise
    0 -> null
    1 -> if (list[0].getPolarAngle()!! >= 0.0) Polygon(list, mutableListOf()) else Polygon(mutableListOf(), list)
    2 -> {
        val angle1 = list[0].getPolarAngle() ?: throw IllegalArgumentException("Wrong arguments")
        val angle2 = list[1].getPolarAngle() ?: throw IllegalArgumentException("Wrong arguments")
        when {
            checkIsLeft(angle1) && checkIsRight(angle2) -> Polygon(mutableListOf(list[0]), mutableListOf(list[1]))
            checkIsRight(angle1) && checkIsLeft(angle2) -> Polygon(mutableListOf(list[1]), mutableListOf(list[0]))
            checkIsLeft(angle1) && checkIsLeft(angle2) -> {
                if (angle1 <= angle2) {
                    Polygon(mutableListOf(list[1], list[0]), mutableListOf())
                } else {
                    Polygon(mutableListOf(list[0], list[1]), mutableListOf())
                }
            }
            checkIsRight(angle1) && checkIsRight(angle2) -> {
                if (angle1 <= angle2) {
                    Polygon(mutableListOf(), mutableListOf(list[0], list[1]))
                } else {
                    Polygon(mutableListOf(), mutableListOf(list[1], list[0]))
                }
            }
            else -> TODO()
        }
    }
    else -> findIntersectionOfPolygons(findIntersection(list.subList(0, list.size / 2)), findIntersection(list.subList(list.size / 2, list.size)))
}

fun findIntersectionOfPolygons(polygon1: Polygon?, polygon2: Polygon?) = when {
    polygon1 == null && polygon2 != null -> polygon2
    polygon1 != null && polygon2 == null -> polygon1
    polygon1 == null && polygon2 == null -> null
    else -> {
        val leftEdges = mutableListOf<HalfPlain>()
        val rightEdges = mutableListOf<HalfPlain>()
        val isManyEdges: (Polygon, Polygon) -> Boolean = { poly1, poly2 ->
            poly1.leftEdges.size + poly1.rightEdges.size + poly2.leftEdges.size + poly2.rightEdges.size > 4
        }

        val arePolygonsNotFull: (Polygon, Polygon) -> Boolean = { poly1, poly2 ->
            poly1.leftEdges.isEmpty() && poly1.rightEdges.isNotEmpty() && poly2.leftEdges.isNotEmpty() && poly2.rightEdges.isEmpty() ||
                    poly1.leftEdges.isNotEmpty() && poly1.rightEdges.isEmpty() && poly2.leftEdges.isEmpty() && poly2.rightEdges.isNotEmpty()
        }
        when {
            !isManyEdges(polygon1!!, polygon2!!) -> {
                leftEdges.addAll((polygon1.leftEdges + polygon2.leftEdges).sortedByDescending { it.getPolarAngle() })
                rightEdges.addAll((polygon1.rightEdges + polygon2.rightEdges).sortedBy { it.getPolarAngle() })
            }
            arePolygonsNotFull(polygon1, polygon2) -> {
                leftEdges.addAll((polygon1.leftEdges + polygon2.leftEdges))
                rightEdges.addAll((polygon1.rightEdges + polygon2.rightEdges))
            }
            else -> {

                val isPointInsideP1: (Point, HalfPlain?, HalfPlain?) -> Boolean = { point, leftEdgeP1, rightEdgeP1 ->
                    leftEdgeP1 != null && rightEdgeP1 != null && leftEdgeP1.isPointInside(point) && rightEdgeP1.isPointInside(point)
                }
                val isPointInsideP2: (Point, HalfPlain?, HalfPlain?) -> Boolean = { point, leftEdgeP2, rightEdgeP2 ->
                    leftEdgeP2 != null && rightEdgeP2 != null && leftEdgeP2.isPointInside(point) && rightEdgeP2.isPointInside(point)
                }

                val isIntersectionInsidePolygonLeftEdge: (Point, Polygon, HalfPlain, Int) -> Boolean =
                    { intersection, polygon, leftEdge, leftEdgeIndex ->
                        intersection.y >= polygon.findBottomCornerOfLeftEdge(leftEdge, leftEdgeIndex).y &&
                                intersection.y <= polygon.findTopCornerOfLeftEdge(leftEdge, leftEdgeIndex).y
                    }

                val isIntersectionInsidePolygonRightEdge: (Point, Polygon, HalfPlain, Int) -> Boolean =
                    { intersection, polygon, rightEdge, rightEdgeIndex ->
                        intersection.y <= polygon.findTopCornerOfRightEdge(rightEdge, rightEdgeIndex).y &&
                                intersection.y >= polygon.findBottomCornerOfRightEdge(rightEdge, rightEdgeIndex).y
                    }

                // Indices of status elements
                var leftEdgeP1Index = 0
                var rightEdgeP1Index = 0
                var leftEdgeP2Index = 0
                var rightEdgeP2Index = 0

                // Current status
                var leftEdgeP1: HalfPlain?
                var rightEdgeP1: HalfPlain?
                var leftEdgeP2: HalfPlain?
                var rightEdgeP2: HalfPlain?

                // Used to traverse
                var leftEdgeP1Top: Point?
                var rightEdgeP1Top: Point?
                var leftEdgeP2Top: Point?
                var rightEdgeP2Top: Point?

                // Used to find next top
                var leftEdgeP1Bottom: Point?
                var rightEdgeP1Bottom: Point?
                var leftEdgeP2Bottom: Point?
                var rightEdgeP2Bottom: Point?

                // Find top - event point and event line (half-plain)
                var lastEventPoint = Point(0.0, 0.0)
                val topEventPointY = min(polygon1.findTop().y, polygon2.findTop().y) // FIXME: both -Infinite
                val endEventPoint = listOf(polygon1.findBottom(), polygon2.findBottom()).minBy { it.y }!!
                val eventPointQueue = mutableListOf<Point>().apply {
                    addAll(listOf(polygon1.findTop(), polygon2.findTop()).filter {
                        it.y != Double.POSITIVE_INFINITY && abs(it.y - topEventPointY) <= EPSILON
                    })
                }

                // Traverse event points
                do {
                    leftEdgeP1 = polygon1.leftEdges.getOrNull(leftEdgeP1Index)
                    rightEdgeP1 = polygon1.rightEdges.getOrNull(rightEdgeP1Index)
                    leftEdgeP2 = polygon2.leftEdges.getOrNull(leftEdgeP2Index)
                    rightEdgeP2 = polygon2.rightEdges.getOrNull(rightEdgeP2Index)

                    leftEdgeP1Top = leftEdgeP1?.let { polygon1.findTopCornerOfLeftEdge(leftEdgeP1, leftEdgeP1Index) }
                    rightEdgeP1Top = rightEdgeP1?.let { polygon1.findTopCornerOfRightEdge(rightEdgeP1, rightEdgeP1Index) }
                    leftEdgeP2Top = leftEdgeP2?.let { polygon2.findTopCornerOfLeftEdge(leftEdgeP2, leftEdgeP2Index) }
                    rightEdgeP2Top = rightEdgeP2?.let { polygon2.findTopCornerOfRightEdge(rightEdgeP2, rightEdgeP2Index) }

                    leftEdgeP1Bottom = leftEdgeP1?.let { polygon1.findBottomCornerOfLeftEdge(it, leftEdgeP1Index) }
                    rightEdgeP1Bottom = rightEdgeP1?.let { polygon1.findBottomCornerOfRightEdge(it, rightEdgeP1Index) }
                    leftEdgeP2Bottom = leftEdgeP2?.let { polygon2.findBottomCornerOfLeftEdge(it, leftEdgeP2Index) }
                    rightEdgeP2Bottom = rightEdgeP2?.let { polygon2.findBottomCornerOfRightEdge(it, rightEdgeP2Index) }

                    for (eventPoint in eventPointQueue) {
                        val currentEventHalfPlain = HalfPlain(Line(0.0, -1.0, eventPoint.y))
                        val edgesToHandle = mutableListOf<HalfPlain>().apply {
                            leftEdgeP1?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                            rightEdgeP1?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                            leftEdgeP2?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                            rightEdgeP2?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                        }

                        edgesToHandle.forEach { currentHalfPlain ->
                            when (currentHalfPlain) {
                                // Working with Left of C1
                                leftEdgeP1 -> {
                                    if (isPointInsideP2(leftEdgeP1Top!!, leftEdgeP2, rightEdgeP2)) leftEdges.add(leftEdgeP1)
                                    if (rightEdgeP2?.isIntersectedBy(leftEdgeP1) != null && !rightEdgeP2.isPointInside(leftEdgeP1Top)) {
                                        val intersection = rightEdgeP2.isIntersectedBy(leftEdgeP1)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon1, leftEdgeP1, leftEdgeP1Index) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon2, rightEdgeP2, rightEdgeP2Index)
                                        ) {
                                            leftEdges.add(leftEdgeP1)
                                            rightEdges.add(rightEdgeP2)
                                        }
                                    }
                                    if (leftEdgeP2?.isIntersectedBy(leftEdgeP1) != null) {
                                        val intersection = leftEdgeP2.isIntersectedBy(leftEdgeP1)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon1, leftEdgeP1, leftEdgeP1Index) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon2, leftEdgeP2, leftEdgeP2Index)
                                        ) if (leftEdgeP2.isPointInside(leftEdgeP1Top)) leftEdges.add(leftEdgeP2) else leftEdges.add(leftEdgeP1)
                                    }
                                }
                                // Working with Right of C1
                                rightEdgeP1 -> {
                                    if (isPointInsideP2(rightEdgeP1Top!!, leftEdgeP2, rightEdgeP2)) rightEdges.add(rightEdgeP1)
                                    if (leftEdgeP2?.isIntersectedBy(rightEdgeP1) != null && !leftEdgeP2.isPointInside(rightEdgeP1Top)) {
                                        val intersection = leftEdgeP2.isIntersectedBy(rightEdgeP1)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon1, rightEdgeP1, rightEdgeP1Index) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon2, leftEdgeP2, leftEdgeP2Index)
                                        ) {
                                            leftEdges.add(leftEdgeP2)
                                            rightEdges.add(rightEdgeP1)
                                        }
                                    }
                                    if (rightEdgeP2?.isIntersectedBy(rightEdgeP1) != null) {
                                        val intersection = rightEdgeP2.isIntersectedBy(rightEdgeP1)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon1, rightEdgeP1, rightEdgeP1Index) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon2, rightEdgeP2, rightEdgeP2Index)
                                        ) if (rightEdgeP2.isPointInside(rightEdgeP1Top)) rightEdges.add(rightEdgeP2) else rightEdges.add(rightEdgeP1)

                                    }
                                }
                                // Working with Left of C2
                                leftEdgeP2 -> {
                                    if (isPointInsideP1(leftEdgeP2Top!!, leftEdgeP1, rightEdgeP1)) leftEdges.add(leftEdgeP2)
                                    if (rightEdgeP1?.isIntersectedBy(leftEdgeP2) != null && !rightEdgeP1.isPointInside(leftEdgeP2Top)) {
                                        val intersection = rightEdgeP1.isIntersectedBy(leftEdgeP2)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon2, leftEdgeP2, leftEdgeP2Index) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon1, rightEdgeP1, rightEdgeP1Index)
                                        ) {
                                            leftEdges.add(leftEdgeP2)
                                            rightEdges.add(rightEdgeP1)
                                        }
                                    }
                                    if (leftEdgeP1?.isIntersectedBy(leftEdgeP2) != null) {
                                        val intersection = leftEdgeP1.isIntersectedBy(leftEdgeP2)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon2, leftEdgeP2, leftEdgeP2Index) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon1, leftEdgeP1, leftEdgeP1Index)
                                        ) if (leftEdgeP1.isPointInside(leftEdgeP2Top)) leftEdges.add(leftEdgeP1) else leftEdges.add(leftEdgeP2)
                                    }
                                }
                                // Working with Right of C2
                                rightEdgeP2 -> {
                                    if (isPointInsideP1(rightEdgeP2Top!!, leftEdgeP1, rightEdgeP1)) rightEdges.add(rightEdgeP2)
                                    if (leftEdgeP1?.isIntersectedBy(rightEdgeP2) != null && !leftEdgeP1.isPointInside(rightEdgeP2Top)) {
                                        val intersection = leftEdgeP1.isIntersectedBy(rightEdgeP2)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon2, rightEdgeP2, rightEdgeP2Index) &&
                                            isIntersectionInsidePolygonLeftEdge(intersection, polygon1, leftEdgeP1, leftEdgeP1Index)
                                        ) {
                                            leftEdges.add(leftEdgeP1)
                                            rightEdges.add(rightEdgeP2)
                                        }
                                    }
                                    if (rightEdgeP1?.isIntersectedBy(rightEdgeP2) != null) {
                                        val intersection = rightEdgeP1.isIntersectedBy(rightEdgeP2)!!
                                        if (currentEventHalfPlain.isPointInside(intersection) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon2, rightEdgeP2, rightEdgeP2Index) &&
                                            isIntersectionInsidePolygonRightEdge(intersection, polygon1, rightEdgeP1, rightEdgeP1Index)
                                        ) if (rightEdgeP1.isPointInside(rightEdgeP2Top)) rightEdges.add(rightEdgeP1) else rightEdges.add(rightEdgeP2)
                                    }
                                }
                            }
                        }
                        lastEventPoint = eventPoint
                    }
                    // Traverse event points of current status
                    eventPointQueue.clear()
                    val nextPointCandidates = mutableListOf<Point>().apply {
                        leftEdgeP1Bottom?.let { add(it) }
                        rightEdgeP1Bottom?.let { add(it) }
                        leftEdgeP2Bottom?.let { add(it) }
                        rightEdgeP2Bottom?.let { add(it) }
                    }
                    if (nextPointCandidates.isEmpty()) break
                    val maxOfNextPointCandidates = nextPointCandidates.maxBy { it.y }?.also { maxPoint ->
                        eventPointQueue.addAll(nextPointCandidates.filter { point -> abs(point.y - maxPoint.y) <= EPSILON })
                    }
                    maxOfNextPointCandidates?.let {
                        if (it.y == leftEdgeP1Bottom?.y) ++leftEdgeP1Index
                        if (it.y == rightEdgeP1Bottom?.y) ++rightEdgeP1Index
                        if (it.y == leftEdgeP2Bottom?.y) ++leftEdgeP2Index
                        if (it.y == rightEdgeP2Bottom?.y) ++rightEdgeP2Index
                    }
                } while (lastEventPoint != endEventPoint)
            }
        }
        Polygon(leftEdges.distinct().toMutableList(), rightEdges.distinct().toMutableList())
    }
}
