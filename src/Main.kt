import entities.HalfPlain
import entities.Line
import entities.Point
import entities.Polygon
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min

//Common
const val EPSILON = 0.0001
// Left
const val TOP_LEFT_ANGLE = 0.0
const val BOTTOM_LEFT_ANGLE = PI - EPSILON
// Right
const val TOP_RIGHT_ANGLE = TOP_LEFT_ANGLE - EPSILON
const val BOTTOM_RIGHT_ANGLE = -PI

fun checkIsLeft(angle: Double) = (angle in TOP_LEFT_ANGLE..BOTTOM_LEFT_ANGLE)

fun checkIsRight(angle: Double) = (angle in BOTTOM_RIGHT_ANGLE..TOP_RIGHT_ANGLE)

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
    1 -> Polygon(list, mutableListOf())
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
        when {
            polygon1!!.leftEdges.size + polygon1.rightEdges.size + polygon2!!.leftEdges.size + polygon2.rightEdges.size <= 4 -> {
                leftEdges.addAll((polygon1.leftEdges + polygon2.leftEdges).sortedByDescending { it.getPolarAngle() })
                rightEdges.addAll((polygon1.rightEdges + polygon2.rightEdges).sortedBy { it.getPolarAngle() })
            }
            polygon1.leftEdges.isEmpty() && polygon1.rightEdges.isNotEmpty() && polygon2.leftEdges.isNotEmpty() && polygon2.rightEdges.isEmpty() ||
                    polygon1.leftEdges.isNotEmpty() && polygon1.rightEdges.isEmpty() && polygon2.leftEdges.isEmpty() && polygon2.rightEdges.isNotEmpty() -> {
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
                val topEventPointY = min(polygon1.findTop().y, polygon2.findTop().y)
                val endEventPointY = min(polygon1.findBottom().y, polygon2.findBottom().y)
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

                    for (eventPoint in eventPointQueue) {
                        val currentEventHalfPlain = HalfPlain(Line(0.0, -1.0, eventPoint.y))
                        val edgesToHandle = mutableListOf<HalfPlain>().apply {
                            leftEdgeP1?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                            rightEdgeP1?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                            leftEdgeP2?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                            rightEdgeP2?.let { if (it.line.isPointInside(eventPoint)) add(it) }
                        }
                        edgesToHandle.forEach { currentHalfPlain ->
                            leftEdgeP1Top = leftEdgeP1?.let { polygon1.findTopCornerOfLeftEdge(leftEdgeP1, leftEdgeP1Index) }
                            rightEdgeP1Top = rightEdgeP1?.let { polygon1.findTopCornerOfRightEdge(rightEdgeP1, rightEdgeP1Index) }
                            leftEdgeP2Top = leftEdgeP2?.let { polygon2.findTopCornerOfLeftEdge(leftEdgeP2, leftEdgeP2Index) }
                            rightEdgeP2Top = rightEdgeP2?.let { polygon2.findTopCornerOfRightEdge(rightEdgeP2, rightEdgeP2Index) }
                            when (currentHalfPlain) {
                                // Working with Left of C1
                                leftEdgeP1 -> leftEdgeP1.also {
                                    if (isPointInsideP2(leftEdgeP1Top!!, leftEdgeP2, rightEdgeP2)) leftEdges.add(it)
                                    if (rightEdgeP2?.isIntersectedBy(it) != null && !rightEdgeP2.isPointInside(leftEdgeP1Top!!)) {
                                        val intersection = rightEdgeP2.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection)) {
                                            leftEdges.add(it)
                                            rightEdges.add(rightEdgeP2)
                                        }
                                    }
                                    if (leftEdgeP2?.isIntersectedBy(it) != null) {
                                        val intersection = leftEdgeP2.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection))
                                            if (leftEdgeP2.isPointInside(leftEdgeP1Top!!)) leftEdges.add(leftEdgeP2) else leftEdges.add(it)
                                    }
                                }
                                // Working with Right of C1
                                rightEdgeP1 -> rightEdgeP1.also {

                                    if (isPointInsideP2(rightEdgeP1Top!!, leftEdgeP2, rightEdgeP2)) rightEdges.add(it)
                                    if (leftEdgeP2?.isIntersectedBy(it) != null && !leftEdgeP2.isPointInside(rightEdgeP1Top!!)) {
                                        val intersection = leftEdgeP2.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection)) {
                                            leftEdges.add(leftEdgeP2)
                                            rightEdges.add(it)
                                        }
                                    }
                                    if (rightEdgeP2?.isIntersectedBy(it) != null) {
                                        val intersection = rightEdgeP2.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection))
                                            if (rightEdgeP2.isPointInside(rightEdgeP1Top!!)) rightEdges.add(rightEdgeP2) else rightEdges.add(it)
                                    }
                                }
                                // Working with Left of C2
                                leftEdgeP2 -> leftEdgeP2.also {
                                    if (isPointInsideP1(leftEdgeP2Top!!, leftEdgeP1, rightEdgeP1)) leftEdges.add(it)
                                    if (rightEdgeP1?.isIntersectedBy(it) != null && !rightEdgeP1.isPointInside(leftEdgeP2Top!!)) {
                                        val intersection = rightEdgeP1.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection)) {
                                            leftEdges.add(it)
                                            rightEdges.add(rightEdgeP1)
                                        }
                                    }
                                    if (leftEdgeP1?.isIntersectedBy(it) != null) {
                                        val intersection = leftEdgeP1.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection))
                                            if (leftEdgeP1.isPointInside(leftEdgeP2Top!!)) leftEdges.add(leftEdgeP1) else leftEdges.add(it)
                                    }
                                }
                                // Working with Right of C2
                                rightEdgeP2 -> rightEdgeP2.also {
                                    if (isPointInsideP1(rightEdgeP2Top!!, leftEdgeP1, rightEdgeP1)) rightEdges.add(it)
                                    if (leftEdgeP1?.isIntersectedBy(it) != null && !leftEdgeP1.isPointInside(rightEdgeP2Top!!)) {
                                        val intersection = leftEdgeP1.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection)) {
                                            leftEdges.add(leftEdgeP1)
                                            rightEdges.add(it)
                                        }
                                    }
                                    if (rightEdgeP1?.isIntersectedBy(it) != null) {
                                        val intersection = rightEdgeP1.isIntersectedBy(it)!!
                                        if (currentEventHalfPlain.isPointInside(intersection))
                                            if (rightEdgeP1.isPointInside(rightEdgeP2Top!!)) rightEdges.add(rightEdgeP1) else rightEdges.add(it)
                                    }
                                }
                            }
                        }
                        lastEventPoint = eventPoint
                    }
                    // Traverse event points of current status
                    eventPointQueue.clear()
                    leftEdgeP1Bottom = leftEdgeP1?.let { polygon1.findBottomCornerOfLeftEdge(it, leftEdgeP1Index) }
                    rightEdgeP1Bottom = rightEdgeP1?.let { polygon1.findBottomCornerOfRightEdge(it, rightEdgeP1Index) }
                    leftEdgeP2Bottom = leftEdgeP2?.let { polygon2.findBottomCornerOfLeftEdge(it, leftEdgeP2Index) }
                    rightEdgeP2Bottom = rightEdgeP2?.let { polygon2.findBottomCornerOfRightEdge(it, rightEdgeP2Index) }
                    // TODO: null checks
                    eventPointQueue.add(mutableListOf<Point>().apply {
                        leftEdgeP1Bottom?.let { add(it) }
                        rightEdgeP1Bottom?.let { add(it) }
                        leftEdgeP2Bottom?.let { add(it) }
                        rightEdgeP2Bottom?.let { add(it) }
                    }.maxBy { it.y }!!)
                    when (eventPointQueue[0].y) {
                        leftEdgeP1Bottom?.y -> ++leftEdgeP1Index
                        rightEdgeP1Bottom?.y -> ++rightEdgeP1Index
                        leftEdgeP2Bottom?.y -> ++leftEdgeP2Index
                        rightEdgeP2Bottom?.y -> ++rightEdgeP2Index
                    }
                } while (lastEventPoint.y != endEventPointY)
            }
        }
        Polygon(leftEdges, rightEdges)
    }
}
