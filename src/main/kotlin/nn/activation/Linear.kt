package nn.activation

class Linear: Activation()
{
    override fun forward(input: Double): Double = input
    override fun backward(input: Double): Double = 1.0
}
