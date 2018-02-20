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
            override fun forward(input: Float): Float = input * 2
            override fun backward(input: Float): Float = input
        }, { _ -> 0.5f })

        layer.weights[0, 0] = 0.56f
        layer.weights[0, 1] = 0.9f
        layer.weights[0, 2] = -0.08f
        layer.weights[0, 2] = 0.12f
        layer.weights[2, 1] = 0.18f
        layer.weights[3, 2] = -0.25f
        layer.weights[1, 1] = 0.4f
        layer.weights[2, 0] = -0.3f

        layer.biases[0] = 0.3f
        layer.biases[1] = -0.2f
        layer.biases[2] = -0.8f
        layer.biases[3] = 1.5f

        val result = layer.forward(toVec(floatArrayOf(0.4f, 0.8f, 0.9f)))
        result.numRows() shouldEqual 1
        result.numCols() shouldEqual 4
        result[0] shouldEqual 1.352f
        Math.abs(result[1] - 0.77) shouldBeLessOrEqualTo 0.00001
        Math.abs(result[2] + 0.326) shouldBeLessOrEqualTo 0.00001
        result[3] shouldEqual 1.875f
    }
}
