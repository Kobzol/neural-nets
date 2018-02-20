package nn.activation

import koma.extensions.map
import nn.DataVector

abstract class Activation
{
    abstract fun forward(input: Float): Float
    abstract fun backward(input: Float): Float

    fun forward(input: DataVector): DataVector
    {
        return input.map { this.forward(it) }
    }
    fun backward(input: DataVector): DataVector
    {
        return input.map { this.backward(it) }
    }
}
