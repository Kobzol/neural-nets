package nn.layer

import nn.activation.Activation
import nn.math.dot

class Perceptron(override val inputSize: Int,
                 override val neuronCount: Int,
                 override val activation: Activation,
                 private val initializer: (inputSize: Int) -> Float) : Layer
{
    override val biases = FloatArray(neuronCount)
    private val weights = List(neuronCount, { FloatArray(inputSize) })

    init
    {
        this.initialize()
    }

    override fun getWeights(neuron: Int): FloatArray = this.weights[neuron]

    override fun forward(data: FloatArray): FloatArray
    {
        return (0 until this.neuronCount).map { neuron ->
            dot(this.weights[neuron], data) + this.biases[neuron]
        }.toFloatArray()
    }

    private fun initialize()
    {
        for (i in 0 until this.neuronCount)
        {
            for (j in 0 until this.inputSize)
            {
                this.getWeights(i)[j] = this.initializer(this.inputSize)
            }
            this.biases[i] = this.initializer(1)
        }
    }
}
