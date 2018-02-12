package main.hebb

import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import nn.Net

class PointCanvas(width: Double, height: Double) : Canvas(width, height)
{
    fun clear()
    {
        this.graphicsContext2D.clearRect(0.0, 0.0, this.width, this.height)
    }

    fun drawPoints(points: List<Point2D>)
    {
        val ctx = this.graphicsContext2D
        ctx.fill = Paint.valueOf(Color.RED.toString())

        for (point in points)
        {
            ctx.fillOval(point.x, point.y, 3.0, 3.0)
        }
    }

    fun drawLine(a: Float, b: Float, c: Float)
    {
        fun getY(x: Float): Float = (-c - a*x) / b

        val ctx = this.graphicsContext2D
        ctx.stroke = Paint.valueOf(Color.RED.toString())
        ctx.lineWidth = 2.0

        ctx.beginPath()
        ctx.moveTo(0.0, getY(0.0f).toDouble())
        ctx.lineTo(this.width, getY(this.width.toFloat()).toDouble())
        ctx.closePath()
        ctx.stroke()
    }

    fun drawNet(net: Net)
    {
        val layer = net.layers[0]
        val a = layer.weights[0]
        val b = layer.weights[1]
        val c = layer.biases[0]

        this.clear()
        this.drawLine(a, b, c)
    }
}
