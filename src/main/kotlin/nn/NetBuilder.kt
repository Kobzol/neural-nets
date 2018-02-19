package nn

import nn.layer.Layer

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
            size = layer.outputs.size
        }

        return Net(layers)
    }
}
