package main.gui

import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import main.geom.Line
import nn.Net

class PointCanvas(width: Double, height: Double) : Canvas(width, height)
{
    var scale = Point2D(width, height)

    fun clear()
    {
        this.graphicsContext2D.clearRect(0.0, 0.0, this.width, this.height)
    }

    fun drawPoints(point: Point2D, color: Color)
    {
        val ctx = this.graphicsContext2D
        ctx.fill = Paint.valueOf(color.toString())

        val scaled = this.scalePoint(point)
        ctx.fillOval(scaled.x, scaled.y, 3.0, 3.0)
    }

    fun drawLine(line: Line)
    {
        val ctx = this.graphicsContext2D
        ctx.stroke = Paint.valueOf(Color.BLUE.toString())
        ctx.lineWidth = 2.0

        val intersects = listOf(
                Line(1.0f, 0.0f, 0.0f),
                Line(0.0f, 1.0f, 0.0f),
                Line(1.0f, 0.0f, -this.width.toFloat()),
                Line(0.0f, 1.0f, -this.height.toFloat())
        )
                .map { line.intersect(it) }
                .filter { it != null && it.x >= 0 && it.x <= this.width && it.y >= 0 && it.y <= this.height }
                .map { it!! }
        println(intersects)

        if (intersects.size > 1)
        {
            ctx.beginPath()
            ctx.moveTo(intersects[0].x, intersects[0].y)
            ctx.lineTo(intersects[1].x, intersects[1].y)
            ctx.closePath()
            ctx.stroke()
        }
    }

    fun drawNet(net: Net)
    {
        val layer = net.layers[0]
        val a = layer.weights[0]
        val b = layer.weights[1]
        val c = layer.biases[0]

        this.drawLine(Line(a, b, c))
    }

    private fun scalePoint(point: Point2D): Point2D
    {
        return Point2D(
                (point.x / this.scale.x) * this.width,
                (point.y / this.scale.y) * this.height
        )
    }
}