package nn.activation

class Sigmoid : Activation()
{
    override fun forward(input: Double): Double
    {
        return (1.0 / (1 + Math.exp(-input)))
    }
    override fun backward(input: Double): Double
    {
        val f = this.forward(input)
        return f * (1.0 - f)
    }
}
