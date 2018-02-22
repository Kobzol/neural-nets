package nn.activation

class Signum: Activation()
{
    override fun forward(input: Double): Double
    {
        return if (input > 0) 1.0 else 0.0
    }
    override fun backward(input: Double): Double
    {
        throw NotImplementedError()
    }
}
