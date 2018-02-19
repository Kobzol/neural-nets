package nn.layer

import nn.activation.Activation
import java.util.*

class MLP(private val inputSize: Int,
          neuronCount: Int,
          private val activation: Activation,
          private val initializer: (inputSize: Int) -> Float) : Layer
{
    override val outputs = FloatArray(neuronCount)
    override val biases = FloatArray(neuronCount)
    override val weights = FloatArray(neuronCount * inputSize)

    init
    {
        this.initialize()
    }

    override fun forward(data: FloatArray)
    {
        for (i in this.outputs.indices)
        {
            val synapses = (0 until this.inputSize)
                    .map { this.weights[i * this.inputSize + it] * data[it] }
                    .sum()

            this.outputs[i] = this.activation.forward(synapses + this.biases[i])
        }
    }

    private fun initialize()
    {
        for (i in this.outputs.indices)
        {
            for (j in 0 until this.inputSize)
            {
                this.weights[i * this.inputSize + j] = this.initializer(this.inputSize)
            }
        }
    }
}
