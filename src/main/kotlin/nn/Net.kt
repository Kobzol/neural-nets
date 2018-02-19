package nn

import nn.layer.Layer

class Net(val layers: List<Layer>)
{
    fun feedForward(inputs: FloatArray): FloatArray
    {
        var data = inputs
        for (layer in this.layers)
        {
            layer.forward(data)
            data = layer.outputs
        }

        return data
    }

    fun getLoss(inputs: List<FloatArray>, outputs: List<FloatArray>): Float
    {
        return inputs.mapIndexed { index, input ->
            outputs[index].zip(this.feedForward(input))
                .map { p -> Math.pow((p.first - p.second).toDouble(), 2.0) }
                .sum()
        }.sum().toFloat()
    }
}
