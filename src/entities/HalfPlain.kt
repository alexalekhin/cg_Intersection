package entities

data class HalfPlain(val line: Line, val isClosed: Boolean = true) {

    fun isPointInside(point: Point) = (line.a * point.x + line.b * point.y + line.c >= 0)
    fun isPointOnLine(point: Point) = line.isPointInside(point)

    fun isIntersectedBy(halfPlain: HalfPlain?) = halfPlain?.line?.isIntersectedBy(line)

    fun getPolarAngle() = line.getPolarAngle()

    companion object {
        @JvmStatic
        fun fromString(halfPlainString: String): HalfPlain? {
            val line = Line.fromString(halfPlainString) ?: return null
            return HalfPlain(line)
        }
    }
}