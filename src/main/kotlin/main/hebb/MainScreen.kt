package main.hebb

import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import main.gui.LossChart
import main.gui.PointCanvas
import main.xml.PerceptronTaskDto
import nn.Net
import nn.createHebbNet
import nn.learn.HebbLearner

class MainScreen(private val perceptronTask: PerceptronTaskDto) : VBox()
{
    private val canvas = PointCanvas(300.0, 300.0)
    private val lossChart = LossChart("Iteration", "Loss")
    private var net: Net = createHebbNet(perceptronTask.perceptron.data.size)
    private var learner: HebbLearner

    init
    {
        this.learner = HebbLearner(this.net, 0.05)

        val learnBtn = Button("Learn one step")
        learnBtn.setOnAction {
            val trainSet = this.perceptronTask.trainSet
            val testSet = this.perceptronTask.testSet
            val trainInputs = trainSet.inputs()
            val trainLabels = trainSet.outputs()

            this.learner.learnBatch(trainInputs, trainLabels)
            this.draw()

            val trainErr = this.net.getLoss(trainInputs, trainLabels)
            this.lossChart.addPoint("Train", trainErr)

            val testErr = this.net.getLoss(testSet.inputs(), testSet.outputs())
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

        this.canvas.drawHebbNet(this.net)
    }
}
