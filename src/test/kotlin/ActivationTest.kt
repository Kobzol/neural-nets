import nn.activation.Activation
import nn.activation.Sigmoid
import nn.layer.Perceptron
import org.amshove.kluent.`should be less or equal to`
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class ActivationTest
{
    @Test
    fun `activation forwards all inputs`()
    {
        val activation = object : Activation() {
            override fun backward(input: Float): Float = input
            override fun forward(input: Float): Float = input * 2
        }

        activation.forward(floatArrayOf(1.0f, 2.0f, 3.0f)) shouldEqual floatArrayOf(2.0f, 4.0f, 6.0f)
    }
}
