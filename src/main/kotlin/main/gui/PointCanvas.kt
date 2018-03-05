package main.gui

import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import koma.create
import koma.extensions.get
import koma.extensions.set
import main.geom.Line
import nn.Net

class PointCanvas(width: Double, height: Double) : Canvas(width, height)
{
    var scale = Point2D(width, height)

    fun clear()
    {
        this.graphicsContext2D.clearRect(0.0, 0.0, this.width, this.height)
    }

    fun drawPoint(point: Point2D, color: Color, scale: Boolean = true)
    {
        val ctx = this.graphicsContext2D
        ctx.fill = Paint.valueOf(color.toString())

        val scaled = if (scale) this.scalePoint(point) else point
        ctx.fillOval(scaled.x, this.height - scaled.y, 3.0, 3.0)
    }

    fun drawLine(line: Line)
    {
        /*val ctx = this.graphicsContext2D
        ctx.stroke = Paint.valueOf(Color.BLUE.toString())
        ctx.lineWidth = 2.0

        val intersects = listOf(
                Line(1.0, 0.0, 0.0),
                Line(0.0, 1.0, 0.0),
                Line(1.0, 0.0, -this.width),
                Line(0.0, 1.0, -this.height)
        )
                .map { line.intersect(it) }
                .filter { it != null && it.x >= 0 && it.x <= this.width && it.y >= 0 && it.y <= this.height }
                .map { it!! }
        println(intersects)

        if (intersects.size > 1)
        {
            ctx.beginPath()
            ctx.moveTo(intersects[0].x, this.height - intersects[0].y)
            ctx.lineTo(intersects[1].x, this.height - intersects[1].y)
            ctx.closePath()
            ctx.stroke()
        }*/

        val bslope = -line.c / line.b
        val mslope = -line.a / line.b

        for (i in 0 until 100)
        {
            val x = this.scale.x * (i / 100.0)
            val result = mslope * x + bslope
            this.drawPoint(Point2D(x.toDouble(), result), Color.BLUE)
        }
    }

    fun drawHebbNet(net: Net)
    {
        val layer = net.layers[0]
        val a = layer.weights[0, 0]
        val b = layer.weights[0, 1]
        val c = layer.biases[0]

        this.drawLine(Line(a, b, c))
    }
    fun drawNet2D(net: Net)
    {
        for (y in 0 until this.height.toInt())
        {
            for (x in 0 until this.width.toInt())
            {
                val point = this.unscalePoint(Point2D(x.toDouble(), y.toDouble()))

                val input = create(doubleArrayOf(point.x, point.y))
                val output = net.forward(input)[0]

                this.graphicsContext2D.pixelWriter.setColor(x, (this.height - y).toInt(), Color.hsb(0.5, output, 1.0))
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
    private fun unscalePoint(point: Point2D): Point2D
    {
        return Point2D(
                (point.x / this.width) * this.scale.x,
                (point.y / this.height) * this.scale.y
        )
    }
}
