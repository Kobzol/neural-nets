package main.backprop

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import koma.extensions.get
import main.gui.LossChart
import main.gui.PointCanvas
import main.xml.BackpropNetDto
import nn.DataVector
import nn.Net
import nn.NetBuilder
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.BackpropLearner
import nn.learn.Learner

class MainScreen(private val backpropTask: BackpropNetDto) : VBox()
{
    private val canvas = PointCanvas(300.0, 300.0)
    private val lossChart = LossChart("Iteration", "Loss")
    private val buttons: MutableList<Button> = mutableListOf()

    private lateinit var net: Net
    private lateinit var learner: Learner

    private var trainInputs: MutableList<DataVector> = mutableListOf()
    private var trainOutputs: MutableList<DataVector> = mutableListOf()
    private var testInputs: MutableList<DataVector> = mutableListOf()
    private var testOutputs: MutableList<DataVector> = mutableListOf()
    private val hasTest = backpropTask.testSet.testElements[0].outputs.values.isNotEmpty()
    private var stepCounter = 0

    init
    {
        val panel = this.createButtonPanel()
        this.children += panel
        this.children += this.canvas
        this.children += lossChart

        this.canvas.scale = Point2D(backpropTask.data.data[0].maximum, backpropTask.data.data[1].maximum)

        this.loadFromFile()

        for (i in 0 until 50000)
        {
            this.learnOneStep(5000)
        }
    }

    private fun createButtonPanel(): Node
    {
        val row = HBox()

        val learnBtn = Button("Step")
        learnBtn.setOnAction { this.learnOneStep() }
        this.buttons += learnBtn

        val learnBatchBtn = Button("250 steps")
        learnBatchBtn.setOnAction {
            this.setButtonsState(false)
            Observable.create<Boolean> { observer ->
                for (i in 0 until 250)
                {
                    this.learnOneStep(125)
                    observer.onNext(true)
                }

                observer.onComplete()
            }.subscribeOn(Schedulers.computation())
            .subscribe({}, {}, {
                println("Completed, error: ${this.net.getLoss(this.trainInputs, this.trainOutputs)}")
                this.setButtonsState(true)
            })
        }
        this.buttons += learnBatchBtn

        val halveBtn = Button("Halve learning rate")
        halveBtn.setOnAction { this.halveLearningRate() }
        this.buttons += halveBtn

        val loadBtn = Button("Load file")
        loadBtn.setOnAction { this.loadFromFile() }
        this.buttons += loadBtn

        val resetBtn = Button("Reset net")
        resetBtn.setOnAction { this.initLearning() }
        this.buttons += resetBtn

        val resetAllBtn = Button("Reset all")
        resetAllBtn.setOnAction { this.reset() }
        this.buttons += resetAllBtn

        this.buttons.forEach {
            row.children += it
        }

        return row
    }

    private fun learnOneStep(everyNthStep: Int = 1)
    {
        this.learner.learnBatch(this.trainInputs, this.trainOutputs)
        this.stepCounter += 1

        if (this.stepCounter % everyNthStep == 0)
        {
            val trainErr = this.net.getLoss(this.trainInputs, this.trainOutputs)
            Platform.runLater {
                this.lossChart.addPoint("Train", trainErr)
                this.drawNet()
            }
        }
    }

    private fun setButtonsState(enabled: Boolean)
    {
        this.buttons.forEach {
            it.isDisable = !enabled
        }
    }

    private fun halveLearningRate()
    {
        this.learner.learningRate /= 2.0
    }

    private fun reset()
    {
        this.trainInputs = mutableListOf()
        this.trainOutputs = mutableListOf()
        this.testInputs = mutableListOf()
        this.testOutputs = mutableListOf()
        this.initLearning()
    }
    private fun initLearning()
    {
        val builder = NetBuilder()
        for (count in this.backpropTask.layers.counts)
        {
            builder.add { s -> Perceptron(s, count, Sigmoid(), createNormalInitializer(0.0, 0.5)) }
        }

        this.stepCounter = 0
        this.net = builder.build(this.backpropTask.data.data.size)
        this.learner = BackpropLearner(this.net, 0.6)

        this.drawNet()
        this.lossChart.reset()
    }
    private fun loadFromFile()
    {
        this.trainInputs = this.backpropTask.trainSet.inputs().toMutableList()
        this.trainOutputs = this.backpropTask.trainSet.outputs().toMutableList()

        this.testInputs = this.backpropTask.testSet.inputs().toMutableList()
        this.testOutputs = this.backpropTask.testSet.outputs().toMutableList()
        this.initLearning()
    }

    private fun drawNet()
    {
        if (this.net.layers[0].inputSize == 2)
        {
            this.canvas.clear()
            this.canvas.drawNet2D(this.net)
            this.trainInputs.forEachIndexed { index, it ->
                this.canvas.drawPoint(
                        Point2D(it[0], it[1]),
                        if (this.trainOutputs[index][0] == 1.0) Color.BLUE else Color.DARKGREEN
                )
            }
            if (this.hasTest)
            {
                this.testInputs.forEachIndexed { index, it ->
                    this.canvas.drawPoint(
                            Point2D(it[0], it[1]),
                            if (this.testOutputs[index][0] == 1.0) Color.BLUE else Color.PURPLE
                    )
                }
            }
        }
    }
}
