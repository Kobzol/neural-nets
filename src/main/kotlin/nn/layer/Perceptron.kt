package nn.layer

import koma.extensions.set
import koma.matrix.Matrix
import koma.matrix.MatrixTypes
import koma.zeros
import nn.activation.Activation

class Perceptron(override val inputSize: Int,
                 override val neuronCount: Int,
                 override val activation: Activation,
                 private val initializer: (inputSize: Int) -> Float) : Layer
{
    override var biases = zeros(1, neuronCount, MatrixTypes.FloatType)
    override var weights = zeros(neuronCount, inputSize, MatrixTypes.FloatType)

    init
    {
        this.initialize()
    }

    override fun forward(data: Matrix<Float>): Matrix<Float>
    {
        return (data * this.weights.transpose()) + this.biases
    }

    private fun initialize()
    {
        for (i in 0 until this.neuronCount)
        {
            for (j in 0 until this.inputSize)
            {
                this.weights[i, j] = this.initializer(this.inputSize)
            }
            this.biases[i] = this.initializer(1)
        }
    }
}
