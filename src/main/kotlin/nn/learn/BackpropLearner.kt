package nn.learn

import nn.Net
import nn.math.sum

class BackpropLearner(private val net: Net,
                      private var learningRate: Float)
{
    fun learnBatch(inputs: List<FloatArray>, labels: List<FloatArray>)
    {
        val weightDeltas = this.net.layers.map { layer ->
            MutableList(layer.neuronCount, { FloatArray(layer.inputSize) })
        }.toList()
        val biasDeltas = this.net.layers.map { FloatArray(it.neuronCount) }.toMutableList()

        for ((input, label) in inputs.zip(labels))
        {
            val (weightDelta, biasDelta) = this.learnSample(input, label)
            for (layer in weightDeltas.indices)
            {
                for (neuron in weightDeltas[layer].indices)
                {
                    weightDeltas[layer][neuron] = sum(weightDelta[layer][neuron], weightDeltas[layer][neuron])
                }
            }
            biasDelta.forEachIndexed { index, value ->
                biasDeltas[index] = sum(biasDeltas[index], value)
            }
        }

        // apply biases
        biasDeltas.zip(this.net.layers).forEach { (delta, layer) ->
            for (i in layer.biases.indices)
            {
                layer.biases[i] -= this.learningRate * delta[i]
            }
        }

        // apply weights
        weightDeltas.zip(this.net.layers).forEach { (delta, layer) ->
            for (neuron in 0 until layer.neuronCount)
            {
                for (input in 0 until layer.inputSize)
                {
                    layer.getWeights(neuron)[input] -= this.learningRate *  delta[neuron][input]
                }
            }
        }
    }

    private fun learnSample(features: FloatArray, label: FloatArray): Pair<List<List<FloatArray>>, List<FloatArray>>
    {
        val (outputs, activations) = this.feedForward(features)

        // derivation of L2 loss function
        var delta = outputs.last().zip(label).mapIndexed { index, (output, label) ->
            (activations.last()[index] - label) * this.net.layers.last().activation.backward(output)
        }.toFloatArray()
        val biasDeltas = mutableListOf(delta)
        val weightDeltas = mutableListOf(deltaToWeightDelta(delta, activations[activations.size - 2]))

        for (i in this.net.layers.size - 2 downTo 0)
        {
            val output = outputs[i]
            val layer = this.net.layers[i]
            val nextLayer = this.net.layers[i + 1]
            val derivedActivation = layer.activation.backward(output)
            delta = (0 until layer.neuronCount).map { input ->
                delta.mapIndexed { index, value ->
                    value * nextLayer.getWeights(index)[input] * derivedActivation[input]
                }.sum()
            }.toFloatArray()
            biasDeltas.add(0, delta)
            weightDeltas.add(0, this.deltaToWeightDelta(delta, activations[i]))
        }

        return Pair(weightDeltas, biasDeltas)
    }

    private fun feedForward(input: FloatArray): Pair<MutableList<FloatArray>, MutableList<FloatArray>>
    {
        val outputs = mutableListOf<FloatArray>()
        val activations = mutableListOf(input)
        var activation = input

        for (layer in this.net.layers)
        {
            val output = layer.forward(activation)
            outputs += output
            activation = layer.activation.forward(output)
            activations += activation
        }
        return Pair(outputs, activations)
    }
    private fun deltaToWeightDelta(delta: FloatArray, activation: FloatArray): List<FloatArray>
    {
        return delta.map { activation
                .map { a -> a * it }
                .toFloatArray()
        }.toList()
    }
}
