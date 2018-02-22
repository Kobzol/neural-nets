package main.mnist

import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import koma.matrix.ejml.EJMLMatrixFactory
import main.gui.LossChart
import mnist.MnistReader
import nn.DataVector
import nn.Net
import nn.NetBuilder
import nn.activation.ReLu
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.SGDLearner
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.File
import javax.imageio.ImageIO


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
fun countCorrect(testInputs: List<DataVector>, testLabels: List<DataVector>, net: Net): Int
{
    return testInputs.zip(testLabels).count { (input, label) ->
        val res = net.forward(input)
        res.argMax() == label.argMax()
    }
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
                val trainInput = readImages("mnist/mnist-train-input.bin").subList(0, 5000)
                val trainLabels = readLabels("mnist/mnist-train-label.bin").subList(0, 5000)
                val testInput = readImages("mnist/mnist-test-input.bin")
                val testLabels = readLabels("mnist/mnist-test-label.bin")

                val net = NetBuilder()
                        .add { s -> Perceptron(s, 15, ReLu(), createNormalInitializer(0.0, 1.0, true)) }
                        .add { s -> Perceptron(s, 10, Sigmoid(), createNormalInitializer(0.0, 1.0, true)) }
                        .build(784)

                val learner = SGDLearner(net, 0.1, 100)
                for (i in 0 until 300)
                {
                    learner.learnBatch(trainInput, trainLabels)

                    val loss = net.getLoss(testInput, testLabels)
                    val correct = countCorrect(testInput, testLabels, net)
                    println("Epoch $i")
                    println("Loss: $loss")
                    println("Correct: $correct")
                    Platform.runLater {
                        chart.addPoint("Loss", loss)
                        chart.addPoint("Correct", correct.toDouble())
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

    val trainInput = readImages("mnist/mnist-train-input.bin").subList(0, 5000)
    val trainLabels = readLabels("mnist/mnist-train-label.bin").subList(0, 5000)
    val testInput = readImages("mnist/mnist-test-input.bin")
    val testLabels = readLabels("mnist/mnist-test-label.bin")

    val net = NetBuilder()
            .add { s -> Perceptron(s, 15, ReLu(), createNormalInitializer(0.0, 1.0, true)) }
            .add { s -> Perceptron(s, 10, Sigmoid(), createNormalInitializer(0.0, 1.0, true)) }
            .build(784)

    val learner = SGDLearner(net, 0.1, 100)
    for (i in 0 until 300)
    {
        learner.learnBatch(trainInput, trainLabels)
        println("Epoch $i")
        println("Loss: ${net.getLoss(testInput, testLabels)}")
        println("Correct: ${countCorrect(testInput, testLabels, net)}")
    }
}
