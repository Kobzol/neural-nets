package main.hebb

import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import main.gui.LossChart
import main.gui.PointCanvas
import main.xml.PerceptronTaskDto
import nn.DataVector
import nn.Net
import nn.NetBuilder
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.BackpropLearner
import nn.learn.Learner

class MainScreen(private val perceptronTask: PerceptronTaskDto) : VBox()
{
    private val canvas = PointCanvas(300.0, 300.0)
    private val lossChart = LossChart("Iteration", "Loss")
    private var net: Net = NetBuilder()
            .add { s -> Perceptron(s, 3, Sigmoid(), createNormalInitializer(0.5, 0.5)) }
            .add { s -> Perceptron(s, 1, Sigmoid(), createNormalInitializer(0.5, 0.5)) }
            .build(perceptronTask.perceptron.data.size)
    private var learner: Learner

    init
    {
        this.learner = BackpropLearner(this.net, 0.3)

        val learnBtn = Button("Learn one step")
        val trainSet = this.perceptronTask.trainSet
        val testSet = this.perceptronTask.testSet
        val trainInputs = this.scaleInputs(trainSet.inputs())
        val trainLabels = trainSet.outputs()

        learnBtn.setOnAction {
            this.learner.learnBatch(trainInputs, trainLabels)
            this.drawSigmoid(this.net)

            val trainErr = this.net.getLoss(trainInputs, trainLabels)
            this.lossChart.addPoint("Train", trainErr)

            val testErr = this.net.getLoss(this.scaleInputs(testSet.inputs()), testSet.outputs())
            this.lossChart.addPoint("Test", testErr)
        }

        this.children += learnBtn
        this.children += this.canvas
        this.children += lossChart

        this.canvas.scale = Point2D(20.0, 20.0)
        this.drawSigmoid(this.net)
    }

    fun drawHebb()
    {
        this.canvas.clear()
        this.perceptronTask.trainSet.elements.forEach {
            this.canvas.drawPoints(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.RED else Color.GREEN
            )
        }
        this.perceptronTask.testSet.elements.forEach {
            this.canvas.drawPoints(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.BLUE else Color.PURPLE
            )
        }

        this.canvas.drawHebbNet(this.net)
    }
    fun drawSigmoid(net: Net)
    {
        this.canvas.clear()
        this.canvas.drawNet2D(net)
        this.perceptronTask.trainSet.elements.forEach {
            this.canvas.drawPoints(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.RED else Color.GREEN
            )
        }
        this.perceptronTask.testSet.elements.forEach {
            this.canvas.drawPoints(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.BLUE else Color.PURPLE
            )
        }
    }

    private fun scaleInputs(inputs: List<DataVector>): List<DataVector>
    {
        return inputs
    }
}
