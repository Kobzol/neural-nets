package main.hebb

import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import koma.create
import koma.extensions.get
import main.gui.LossChart
import main.gui.PointCanvas
import main.xml.PerceptronTaskDto
import nn.DataVector
import nn.Net
import nn.createHebbNet
import nn.learn.HebbLearner
import nn.learn.Learner

class MainScreen(private val perceptronTask: PerceptronTaskDto) : VBox()
{
    private val canvas = PointCanvas(300.0, 300.0)
    private val lossChart = LossChart("Iteration", "Loss")
    private lateinit var net: Net
    private lateinit var learner: Learner

    private var trainInputs: List<DataVector> = listOf()
    private var trainOutputs: List<DataVector> = listOf()
    private var testInputs: List<DataVector> = listOf()
    private var testOutputs: List<DataVector> = listOf()

    init
    {
        val panel = this.createButtonPanel()
        this.children += panel
        this.children += this.canvas
        this.children += lossChart

        this.canvas.scale = Point2D(20.0, 20.0)
        this.canvas.setOnMouseClicked {
            val positive = it.button == MouseButton.PRIMARY
            val x = (it.x / this.canvas.width) * this.canvas.scale.x
            val y = ((this.canvas.height - it.y) / this.canvas.height) * this.canvas.scale.y
            this.addTrainExample(x, y, if (positive) 1.0 else 0.0)
        }

        this.loadFromFile()
    }

    private fun addTrainExample(x: Double, y: Double, output: Double)
    {
        this.trainInputs += create(doubleArrayOf(x, y))
        this.trainOutputs += create(doubleArrayOf(output))
        this.drawHebb()
    }

    private fun createButtonPanel(): Node
    {
        val row = HBox()

        val learnBtn = Button("Step")
        learnBtn.setOnAction {
            this.learner.learnBatch(this.trainInputs, this.trainOutputs)
            this.drawHebb()

            val trainErr = this.net.getLoss(this.trainInputs, this.trainOutputs)
            this.lossChart.addPoint("Train", trainErr)

            val testErr = this.net.getLoss(this.testInputs, this.testOutputs)
            this.lossChart.addPoint("Test", testErr)
        }
        row.children += learnBtn

        val loadBtn = Button("Load file")
        loadBtn.setOnAction { this.loadFromFile() }
        row.children += loadBtn

        val resetBtn = Button("Reset net")
        resetBtn.setOnAction { this.initLearning() }
        row.children += resetBtn

        val resetAllBtn = Button("Reset all")
        resetAllBtn.setOnAction { this.reset() }
        row.children += resetAllBtn

        return row
    }

    private fun reset()
    {
        this.trainInputs = listOf()
        this.trainOutputs = listOf()
        this.testInputs = listOf()
        this.testOutputs = listOf()
        this.initLearning()
    }
    private fun initLearning()
    {
        this.net = createHebbNet(2)
        this.learner = HebbLearner(this.net, 0.1)

        this.drawHebb()
        this.lossChart.reset()
    }
    private fun loadFromFile()
    {
        this.trainInputs = this.perceptronTask.trainSet.inputs()
        this.trainOutputs = this.perceptronTask.trainSet.outputs()
        this.testInputs = this.perceptronTask.testSet.inputs()
        this.testOutputs = this.perceptronTask.testSet.outputs()
        this.initLearning()
    }

    private fun drawHebb()
    {
        this.canvas.clear()
        this.trainInputs.forEachIndexed { index, it ->
            this.canvas.drawPoint(
                    Point2D(it[0], it[1]),
                    if (this.trainOutputs[index][0] == 1.0) Color.RED else Color.GREEN
            )
        }
        this.testInputs.forEachIndexed { index, it ->
            this.canvas.drawPoint(
                    Point2D(it[0], it[1]),
                    if (this.testOutputs[index][0] == 1.0) Color.BLUE else Color.PURPLE
            )
        }

        this.canvas.drawHebbNet(this.net)
        println("Error: ${this.net.getLoss(this.trainInputs, this.trainOutputs)}")
    }
    /*fun drawSigmoid(net: Net)
    {
        this.canvas.clear()
        this.canvas.drawNet2D(net)
        this.perceptronTask.trainSet.elements.forEach {
            this.canvas.drawPoint(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.RED else Color.GREEN
            )
        }
        this.perceptronTask.testSet.elements.forEach {
            this.canvas.drawPoint(
                    Point2D(it.inputs.values[0].toDouble(), it.inputs.values[1].toDouble()),
                    if (it.output == 1.0f) Color.BLUE else Color.PURPLE
            )
        }
    }*/
}
