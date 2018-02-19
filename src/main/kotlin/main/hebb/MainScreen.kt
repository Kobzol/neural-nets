package main.hebb

import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import main.geom.Line
import main.gui.LossChart
import main.gui.PointCanvas
import main.xml.PerceptronTaskDto
import nn.Net
import nn.NetBuilder
import nn.activation.Signum
import nn.layer.MLP
import nn.learn.HebbLearner
import java.util.*

class MainScreen(private val perceptronTask: PerceptronTaskDto) : VBox()
{
    private val canvas = PointCanvas(300.0, 300.0)
    private val lossChart = LossChart("Iteration", "Loss")
    private var net: Net
    private var learner: HebbLearner

    init
    {
        val builder = NetBuilder()
        val random = Random()
        builder.add { s -> MLP(s, 1, Signum(), { inputSize ->
            (random.nextGaussian() * 0.5  + 0.5).toFloat()
        }) }
        this.net = builder.build(perceptronTask.perceptron.data.size)
        this.learner = HebbLearner(this.net, 0.01f)

        val learnBtn = Button("Learn one step")
        learnBtn.setOnAction {
            val trainSet = this.perceptronTask.trainSet
            val testSet = this.perceptronTask.testSet

            this.learner.learnBatch(
                    trainSet.elements.map { it.inputs.values.toFloatArray() },
                    trainSet.elements.map { floatArrayOf(it.output) }
            )
            this.draw()
            val trainErr = this.net.getLoss(
                    trainSet.elements.map { it.inputs.values.toFloatArray() },
                    trainSet.elements.map { floatArrayOf(it.output) }
            )
            this.lossChart.addPoint("Train", trainErr)
            val testErr = this.net.getLoss(
                    testSet.elements.map { it.inputs.values.toFloatArray() },
                    testSet.elements.map { floatArrayOf(it.output) }
            )
            this.lossChart.addPoint("Test", testErr)
        }

        this.children += learnBtn
        this.children += this.canvas
        this.children += lossChart

        this.canvas.scale = Point2D(20.0, 20.0)
        this.draw()
    }

    fun draw()
    {
        this.canvas.clear()
        val points = this.perceptronTask.testSet.elements + this.perceptronTask.trainSet.elements
        points.forEach {
            this.canvas.drawPoints(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.RED else Color.GREEN
            )
        }

        this.canvas.drawNet(this.net)
    }
}
