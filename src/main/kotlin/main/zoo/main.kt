package main.zoo

import com.opencsv.CSVReader
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage
import koma.ceil
import koma.min
import main.gui.LossChart
import nn.*
import nn.activation.ReLu
import nn.activation.Sigmoid
import nn.layer.Perceptron
import nn.learn.BackpropLearner
import nn.learn.Learner
import nn.learn.SGDLearner
import java.io.FileReader
import kotlin.math.ceil

class ZooApp: Application()
{
    override fun start(stage: Stage)
    {
        val root = Group()
        stage.scene = Scene(root)
        stage.title = "Animal recognition"

        val container = VBox()
        root.children.add(container)

        val chart = LossChart("Iteration", "Loss")
        container.children.add(chart)

        stage.show()

        val thread = object : Thread() {
            override fun run() {
                val reader = CSVReader(FileReader("zoo/zoo.csv"))
                val entries = reader.readAll().drop(1)

                val inputs = entries.map {
                    toVec(it.slice(1 until it.size - 1).map { it.toFloat() }.toFloatArray())
                }.toMutableList()
                val outputs = entries.map {
                    val classIndex = it.last().toInt() - 1
                    val label = FloatArray(7, { 0.0f })
                    label[classIndex] = 1.0f
                    toVec(label)
                }.toMutableList()

                shuffleMultiple(inputs, outputs)

                val folds = 5
                val net = NetBuilder()
                        .add { s -> Perceptron(s, 32, Sigmoid(), createNormalInitializer()) }
                        .add { s -> Perceptron(s, outputs[0].numCols(), Sigmoid(), createNormalInitializer()) }
                        .build(inputs[0].numCols())
                val learner = SGDLearner(net, 0.05, 10)

                for (e in 0 until 100)
                {
                    for (i in 0 until folds)
                    {
                        val sets = crossValidate(inputs, outputs, i, folds, net, learner)
                        val trainLoss = net.getLoss(sets[0], sets[1])
                        val validationLoss = net.getLoss(sets[2], sets[3])
                        val correct = net.countCorrect(sets[2], sets[3])

                        println("Train loss: $trainLoss")
                        println("Validation loss: $validationLoss")
                        println("Correct: $correct/${sets[2].size}")

                        Platform.runLater {
                            chart.addPoint("Train loss", trainLoss)
                            chart.addPoint("Validation loss", validationLoss)
                            chart.addPoint("Correct", correct.toDouble())
                        }
                    }
                }
            }
        }
        thread.start()
    }

    fun crossValidate(inputs: List<DataVector>, outputs: List<DataVector>,
                      fold: Int, folds: Int,
                      net: Net, learner: Learner): Array<List<DataVector>>
    {
        val validationSize = ceil(inputs.size / folds).toInt()
        val validationStart = validationSize * fold
        val validationEnd = min(inputs.size, validationStart + validationSize)

        val validationInputs = inputs.slice(validationStart until validationEnd)
        val validationOutputs = outputs.slice(validationStart until validationEnd)

        val trainInputs = inputs.slice(0 until validationStart) + inputs.slice(validationEnd until inputs.size)
        val trainOutputs = outputs.slice(0 until validationStart) + inputs.slice(validationEnd until inputs.size)

        assert(trainInputs.size + validationInputs.size == inputs.size)
        assert(trainOutputs.size + validationOutputs.size == outputs.size)

        for (i in 0 until 500)
        {
            learner.learnBatch(trainInputs, trainOutputs)
        }

        return arrayOf(
                trainInputs,
                trainOutputs,
                validationInputs,
                validationOutputs
        )
    }
}

fun main(args: Array<String>)
{
    Application.launch(ZooApp::class.java, *args)
}
