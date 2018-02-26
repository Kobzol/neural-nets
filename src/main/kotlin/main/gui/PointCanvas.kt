package main.gui

import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import koma.create
import koma.extensions.get
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

    fun drawHebbNet(net: Net)
    {
        val layer = net.layers[0]
        val a = layer.weights[0, 0]
        val b = layer.weights[0, 1]
        val c = layer.biases[0]

        this.drawLine(Line(a.toFloat(), b.toFloat(), c.toFloat()))
    }
    fun drawNet2D(net: Net)
    {
        for (y in 0 until this.height.toInt())
        {
            for (x in 0 until this.width.toInt())
            {
                val input = create(doubleArrayOf(x.toDouble(), y.toDouble()))
                val output = net.forward(input)[0]

                this.graphicsContext2D.pixelWriter.setColor(x, y, Color.hsb(0.5, output, 1.0))
            }
        }
    }

    private fun scalePoint(point: Point2D): Point2D
    {
        return Point2D(
                (point.x / this.scale.x) * this.width,
                (point.y / this.scale.y) * this.height
        )
    }
}
