package nn.learn

import nn.DataVector
import nn.Net

class HebbLearner(private val net: Net,
                  override var learningRate: Double): Learner
{
    override fun learnBatch(inputs: List<DataVector>, labels: List<DataVector>)
    {
        inputs.zip(labels).forEach { (input, label) ->
            this.learnSample(input, label)
        }
    }

    private fun learnSample(input: DataVector, label: DataVector)
    {
        val layer = this.net.layers[0]
        val output = layer.activation.forward(layer.forward(input))
        val diff = (label - output) * this.learningRate
        val deltas = diff * input

        layer.weights += deltas
        layer.biases += diff
    }
}
