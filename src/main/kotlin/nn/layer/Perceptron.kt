package nn.layer

import koma.extensions.set
import koma.matrix.Matrix
import koma.matrix.ejml.EJMLMatrixFactory
import nn.DataVector
import nn.activation.Activation

class Perceptron(override val inputSize: Int,
                 override val neuronCount: Int,
                 override val activation: Activation,
                 private val initializer: (inputSize: Int) -> Float) : Layer
{
    override var biases = EJMLMatrixFactory().zeros(1, neuronCount) as Matrix<Double>
    override var weights = EJMLMatrixFactory().zeros(neuronCount, inputSize) as Matrix<Double>

    init
    {
        this.initialize()
    }

    override fun forward(data: DataVector): DataVector
    {
        return (data * this.weights.transpose()) + this.biases
    }

    private fun initialize()
    {
        for (i in 0 until this.neuronCount)
        {
            for (j in 0 until this.inputSize)
            {
                this.weights[i, j] = this.initializer(this.inputSize).toDouble()
            }
            this.biases[i] = this.initializer(1).toDouble()
        }
    }
}
