package nn.learn

import nn.Net

class HebbLearner(private val net: Net,
                  private var learningRate: Float)
{
    fun learnSample(input: FloatArray, output: FloatArray)
    {
        val layer = this.net.layers[0]

        val change = layer.activation.forward(layer.forward(input))
                .zip(output)
                .map { (activation, expected) -> expected - activation }
                .map { it * this.learningRate }

        for (o in change.indices)
        {
            for (i in input.indices)
            {
                layer.getWeights(o)[i] += change[o] * input[i]
            }
        }

        layer.biases.mapIndexed { index, _ ->
            layer.biases[index] += change[index]
        }
    }

    fun learnBatch(inputs: List<FloatArray>, outputs: List<FloatArray>)
    {
        inputs.zip(outputs).forEach { (input, output) ->
            this.learnSample(input, output)
        }
    }
}
