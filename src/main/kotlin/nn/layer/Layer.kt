package nn.layer

import nn.activation.Activation

interface Layer
{
    val biases: FloatArray
    val activation: Activation
    val inputSize: Int
    val neuronCount: Int

    fun forward(data: FloatArray): FloatArray

    fun getWeights(neuron: Int): FloatArray
}
