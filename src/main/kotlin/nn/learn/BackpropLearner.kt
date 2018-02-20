package nn.learn

import koma.matrix.MatrixTypes
import koma.zeros
import nn.DataVector
import nn.Net

class BackpropLearner(private val net: Net,
                      private var learningRate: Float)
{
    fun learnBatch(inputs: List<DataVector>, labels: List<DataVector>)
    {
        val weightDeltas = this.net.layers.map {
            zeros(it.neuronCount, it.inputSize, MatrixTypes.FloatType)
        }.toMutableList()
        val biasDeltas = this.net.layers.map {
            zeros(1, it.neuronCount, MatrixTypes.FloatType)
        }.toMutableList()

        for ((input, label) in inputs.zip(labels))
        {
            val (weightDelta, biasDelta) = this.learnSample(input, label)
            for (layer in weightDeltas.indices)
            {
                weightDeltas[layer] += weightDelta[layer]
            }
            for (layer in biasDeltas.indices)
            {
                biasDeltas[layer] += biasDelta[layer]
            }
        }

        // apply biases
        for (layer in biasDeltas.indices)
        {
            this.net.layers[layer].biases -= biasDeltas[layer].times(this.learningRate)
        }

        // apply weights
        for (layer in weightDeltas.indices)
        {
            this.net.layers[layer].weights -= weightDeltas[layer].times(this.learningRate)
        }
    }

    private fun learnSample(features: DataVector, label: DataVector): Pair<List<DataVector>, List<DataVector>>
    {
        val (outputs, activations) = this.feedForward(features)

        // derivation of L2 loss function
        var delta = (activations.last() - label)
                .elementTimes(this.net.layers.last().activation.backward(outputs.last()))

        val biasDeltas = mutableListOf(delta)
        val weightDeltas = mutableListOf(delta.transpose() * activations[activations.size - 2])

        for (i in this.net.layers.size - 2 downTo 0)
        {
            val output = outputs[i]
            val layer = this.net.layers[i]
            val nextLayer = this.net.layers[i + 1]
            val derivedActivation = layer.activation.backward(output)
            delta = (delta * nextLayer.weights).elementTimes(derivedActivation)
            biasDeltas.add(0, delta)
            weightDeltas.add(0, delta.transpose() * activations[i])
        }

        return Pair(weightDeltas, biasDeltas)
    }

    private fun feedForward(input: DataVector): Pair<List<DataVector>, List<DataVector>>
    {
        val outputs = mutableListOf<DataVector>()
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
}