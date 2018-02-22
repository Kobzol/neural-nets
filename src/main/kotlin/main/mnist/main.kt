package main.mnist

import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import koma.create
import koma.matrix.MatrixTypes
import mnist.MnistReader
import nn.DataVector
import nn.Net
import nn.NetBuilder
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.SGDLearner
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.Raster
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

        create(arr, MatrixTypes.FloatType)
    }.toList()
}
fun readLabels(path: String): List<DataVector>
{
    val labels = MnistReader.getLabels(path)
    return labels.map {
        val label = DoubleArray(10)
        label[it] = 1.0
        create(label, MatrixTypes.FloatType)
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

        val canvas = ImageView()
        canvas.fitWidth = 100.0
        canvas.fitHeight = 100.0
        container.children.add(canvas)

        val trainInput = readImages("mnist/mnist-train-input.bin").subList(0, 1000).toMutableList()
        val trainLabels = readLabels("mnist/mnist-train-label.bin").subList(0, 1000).toMutableList()
        val testInput = readImages("mnist/mnist-test-input.bin").toMutableList()
        val testLabels = readLabels("mnist/mnist-test-label.bin").toMutableList()

        val button = Button("test")
        var i = 0
        button.setOnAction {
            val input = testInput[i]
            val data = input.toIterable().map {
                (it * 255.0f).toByte()
            }.toByteArray()
            val buffer = DataBufferByte(data, data.size)
            val model = ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), intArrayOf(8), false, true,
                    Transparency.OPAQUE, DataBuffer.TYPE_BYTE)
            val sm = model.createCompatibleSampleModel(28, 28)
            val raster = Raster.createWritableRaster(sm, buffer, null)
            val buffered = BufferedImage(model, raster, false, null)

            ImageIO.write(buffered, "jpg", File("test.jpg"))
            val img = SwingFXUtils.toFXImage(buffered, null)
            canvas.image = img
            i++
        }
        container.children.add(button)

        stage.show()
    }
}

fun main(args: Array<String>)
{
    val trainInput = readImages("mnist/mnist-train-input.bin").subList(0, 1000).toMutableList()
    val trainLabels = readLabels("mnist/mnist-train-label.bin").subList(0, 1000).toMutableList()
    val testInput = readImages("mnist/mnist-test-input.bin").toMutableList()
    val testLabels = readLabels("mnist/mnist-test-label.bin").toMutableList()

    val net = NetBuilder()
            .add { s -> Perceptron(s, 30, Sigmoid(), createNormalInitializer(0.0, 1.0, true)) }
            .add { s -> Perceptron(s, 10, Sigmoid(), createNormalInitializer(0.0, 1.0, true)) }
            .build(784)

    val learner = SGDLearner(net, 0.1f, 10)
    for (i in 0 until 300)
    {
        learner.learnBatch(trainInput, trainLabels)
        //println(net.getLoss(testInput, testLabels))
        println(countCorrect(trainInput, trainLabels, net))
    }
}
