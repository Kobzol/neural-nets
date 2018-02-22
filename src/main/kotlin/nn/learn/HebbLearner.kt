package nn.learn

import koma.matrix.ejml.backend.times
import nn.DataVector
import nn.Net

class HebbLearner(private val net: Net,
                  private var learningRate: Float)
{
    fun learnSample(input: DataVector, label: DataVector)
    {
        val layer = this.net.layers[0]
        val output = layer.activation.forward(layer.forward(input))
        val diff = (label - output) * this.learningRate.toDouble()
        val deltas = diff * input

        layer.weights += deltas
        layer.biases += diff
    }

    fun learnBatch(inputs: List<DataVector>, labels: List<DataVector>)
    {
        inputs.zip(labels).forEach { (input, label) ->
            this.learnSample(input, label)
        }
    }
}
