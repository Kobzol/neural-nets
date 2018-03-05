package main.geom

import javafx.geometry.Point2D
import java.lang.Double.isNaN

class Line(val a: Double, val b: Double, val c: Double)
{
    fun intersect(line: Line): Point2D?
    {
        val d = line.a
        val e = line.b
        val f = line.c

        val y = (f - ((c * d) / a)) / (-e + ((b * d) / a))
        if (isNaN(y)) return null

        val x = (-b * y - c) / a
        if (isNaN(x)) return null

        return Point2D(x, y)
    }
}
