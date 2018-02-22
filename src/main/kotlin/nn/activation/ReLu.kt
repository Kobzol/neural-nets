package nn.activation

import kotlin.math.max

class ReLu : Activation()
{
    override fun forward(input: Double): Double
    {
        return max(0.0, input)
    }
    override fun backward(input: Double): Double
    {
        return if (input <= 0) 0.0 else 1.0
    }
}
