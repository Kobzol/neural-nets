package nn

import nn.activation.Signum
import nn.layer.Layer
import nn.layer.Perceptron
import java.util.*

class NetBuilder
{
    private val layers = mutableListOf<(Int) -> Layer>()

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

        return Net(layers)
    }
}

fun createHebbNet(inputSize: Int): Net
{
    return NetBuilder()
            .add { s -> Perceptron(s, 1, Signum(), createNormalInitializer(0.5, 0.5)) }
            .build(inputSize)
}

fun createNormalInitializer(mean: Double = 0.0, variance: Double = 1.0): (Int) -> Float
{
    val random = Random()
    return { (random.nextGaussian() * variance + mean).toFloat() }
}
