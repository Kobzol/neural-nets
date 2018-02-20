package nn.layer

import nn.DataVector
import nn.activation.Activation

interface Layer
{
    var biases: DataVector
    var weights: DataVector
    val activation: Activation
    val inputSize: Int
    val neuronCount: Int

    fun forward(data: DataVector): DataVector
}
