package main.mnist

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage
import koma.matrix.ejml.EJMLMatrixFactory
import main.gui.LossChart
import mnist.MnistReader
import nn.*
import nn.activation.ReLu
import nn.activation.Sigmoid
import nn.layer.Perceptron
import nn.learn.SGDLearner


fun readImages(path: String): List<DataVector>
{
    val images = MnistReader.getImages(path)
    return images.map {
        val arr = DoubleArray(784)
        for (i in it.indices)
        {
            for (j in it[i].indices)
            {
                arr[i * it[i].size + j] = (255.0 - it[i][j].toDouble()) / 255.0
            }
        }

        EJMLMatrixFactory().create(arr)
    }.toList()
}
fun readLabels(path: String): List<DataVector>
{
    val labels = MnistReader.getLabels(path)
    return labels.map {
        val label = DoubleArray(10)
        label[it] = 1.0
        EJMLMatrixFactory().create(label)
    }.toList()
}

class MnistApp: Application()
{
    override fun start(stage: Stage)
    {
        val root = Group()
        stage.scene = Scene(root)
        stage.title = "MNIST recognition"

        val container = VBox()
        root.children.add(container)

        val chart = LossChart("Iteration", "Loss")
        container.children.add(chart)

        stage.show()

        val thread = object : Thread() {
            override fun run() {
                val trainInput = readImages("mnist/mnist-train-input.bin")//.subList(0, 5000)
                val trainLabels = readLabels("mnist/mnist-train-label.bin")//.subList(0, 5000)
                val testInput = readImages("mnist/mnist-test-input.bin")
                val testLabels = readLabels("mnist/mnist-test-label.bin")

                val net = NetBuilder()
                        .add { s -> Perceptron(s, 30, ReLu(), createNormalInitializer(0.0, 1.0, true)) }
                        .add { s -> Perceptron(s, 10, Sigmoid(), createNormalInitializer(0.0, 1.0, true)) }
                        .build(784)

                val learner = SGDLearner(net, 0.05, 10)
                for (i in 0 until 300)
                {
                    println("Epoch $i")

                    profile("Learn", {
                        learner.learnBatch(trainInput, trainLabels)
                    })

                    if (i % 10 == 0)
                    {
                        val loss = net.getLoss(testInput, testLabels)
                        val correct = net.countCorrect(testInput, testLabels)

                        println("Loss: $loss")
                        println("Correct: $correct")

                        Platform.runLater {
                            chart.addPoint("Loss", loss)
                            chart.addPoint("Correct", correct.toDouble())
                        }
                    }
                    if (i > 0 && i % 50 == 0)
                    {
                        learner.learningRate *= 0.5f
                    }
                }
            }
        }
        thread.start()
    }
}

fun main(args: Array<String>)
{
    Application.launch(MnistApp::class.java, *args)
}
