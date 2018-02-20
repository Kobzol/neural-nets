package nn.activation

class Sigmoid : Activation()
{
    override fun forward(input: Float): Float
    {
        return (1.0f / (1 + Math.exp(-input.toDouble()))).toFloat()
    }
    override fun backward(input: Float): Float
    {
        val f = this.forward(input)
        return f * (1.0f - f)
    }
}
