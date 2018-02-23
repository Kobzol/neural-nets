import koma.extensions.get
import koma.extensions.set
import nn.activation.Activation
import nn.layer.Perceptron
import nn.toVec
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class PerceptronTest
{
    @Test
    fun `layer initializes weights`()
    {
        val layer = Perceptron(3, 4, mock(Activation::class), { _ -> 0.5f })
        layer.weights[0, 0] shouldEqual 0.5f
        layer.weights[3, 2] shouldEqual 0.5f
    }

    @Test
    fun `layer forwards`()
    {
        val layer = Perceptron(3, 4, object : Activation() {
            override fun forward(input: Double): Double = input * 2.0
            override fun backward(input: Double): Double = input
        }, { _ -> 0.5f })

        layer.weights[0, 0] = 0.56
        layer.weights[0, 1] = 0.9
        layer.weights[0, 2] = -0.08
        layer.weights[0, 2] = 0.12
        layer.weights[2, 1] = 0.18
        layer.weights[3, 2] = -0.25
        layer.weights[1, 1] = 0.4
        layer.weights[2, 0] = -0.3

        layer.biases[0] = 0.3
        layer.biases[1] = -0.2
        layer.biases[2] = -0.8
        layer.biases[3] = 1.5

        val result = layer.forward(toVec(floatArrayOf(0.4f, 0.8f, 0.9f)))
        result.numRows() shouldEqual 1
        result.numCols() shouldEqual 4
        result[0] shouldEqual 1.352f
        Math.abs(result[1] - 0.77) shouldBeLessOrEqualTo 0.00001
        Math.abs(result[2] + 0.326) shouldBeLessOrEqualTo 0.00001
        result[3] shouldEqual 1.875f
    }
}
