package nn.learn

import nn.Net

class HebbLearner(private val net: Net,
                  private val rate: Float)
{
    public fun learnOneStep(input: FloatArray, groundTruth: FloatArray)
    {
        val layer = this.net.layers[0]
        layer.forward(input)

        val change = layer.outputs
                .zip(groundTruth)
                .map { pair -> pair.second - pair.first }
                .map { it * this.rate }

        for (o in layer.outputs.indices)
        {
            for (i in input.indices)
            {
                layer.weights[o * input.size + i] += change[o] * input[i]
            }
        }

        layer.biases.mapIndexed { index, value ->
            layer.biases[index] += change[index]
        }
    }
}
