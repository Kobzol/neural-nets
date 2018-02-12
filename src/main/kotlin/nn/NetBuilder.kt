package nn

import nn.layer.Layer

class NetBuilder
{
    private val layers = mutableListOf<(Int) -> Layer>()

    public fun add(layerCreator: (Int) -> Layer)
    {
        this.layers += layerCreator
    }
    public fun add(layer: Layer)
    {
        this.layers += { layer }
    }

    public fun build(inputSize: Int): Net
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
