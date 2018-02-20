package nn

import nn.layer.Layer

class Net(val layers: List<Layer>)
{
    fun feedForward(inputs: FloatArray): FloatArray
    {
        var data = inputs
        for (layer in this.layers)
        {
            data = layer.activation.forward(layer.forward(data))
        }

        return data
    }

    fun getLoss(inputs: List<FloatArray>, outputs: List<FloatArray>): Float
    {
        return inputs.mapIndexed { index, input ->
            outputs[index].zip(this.feedForward(input))
                .map { (label, activation) -> Math.pow((label - activation).toDouble(), 2.0) }
                .sum()
        }.sum().toFloat()
    }
}
