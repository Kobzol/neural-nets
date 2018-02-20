package nn.activation

abstract class Activation
{
    abstract fun forward(input: Float): Float
    abstract fun backward(input: Float): Float

    fun forward(input: FloatArray): FloatArray
    {
        return input.map { this.forward(it) }.toFloatArray()
    }
    fun backward(input: FloatArray): FloatArray
    {
        return input.map { this.backward(it) }.toFloatArray()
    }
}
