package nn

import nn.activation.Signum
import nn.layer.Layer
import nn.layer.Perceptron
import nn.loss.HebbLoss
import nn.loss.Loss
import nn.loss.QuadraticLoss
import java.util.*

class NetBuilder
{
    private val layers = mutableListOf<(Int) -> Layer>()
    private var loss: Loss = QuadraticLoss()

    fun add(layerCreator: (Int) -> Layer): NetBuilder
    {
        this.layers += layerCreator
        return this
    }

    fun add(layer: Layer): NetBuilder
    {
        this.layers += { layer }
        return this
    }

    fun loss(loss: Loss): NetBuilder
    {
        this.loss = loss
        return this
    }

    fun build(inputSize: Int): Net
    {
        val layers = mutableListOf<Layer>()
        var size = inputSize

        for (creator in this.layers)
        {
            val layer = creator(size)
            layers += layer
            size = layer.neuronCount
        }

        return Net(layers, this.loss)
    }
}

fun createHebbNet(inputSize: Int): Net
{
    return NetBuilder()
            .add { s -> Perceptron(s, 1, Signum(), createNormalInitializer(0.5, 0.25)) }
            .loss(HebbLoss())
            .build(inputSize)
}

fun createNormalInitializer(mean: Double = 0.0, variance: Double = 1.0, scaleToSize: Boolean = false): (Int) -> Float
{
    val random = Random()
    return { size ->
        val value = (random.nextGaussian() * variance + mean).toFloat()

        if (scaleToSize) (value / Math.sqrt(size.toDouble())).toFloat() else value
    }
}
