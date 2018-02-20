package nn.activation

class Signum: Activation()
{
    override fun forward(input: Float): Float
    {
        return if (input > 0) 1.0f else 0.0f
    }
    override fun backward(input: Float): Float
    {
        throw NotImplementedError()
    }
}
