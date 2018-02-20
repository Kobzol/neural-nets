import nn.Net
import nn.activation.Activation
import nn.activation.Sigmoid
import nn.layer.Perceptron
import org.amshove.kluent.`should be less or equal to`
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class NetTest
{
    @Test
    fun `net calculates loss`()
    {
        val net = Net(listOf(Perceptron(
                2, 2, Sigmoid(), { 0.5f }
        )))

        Math.abs(net.getLoss(
                listOf(floatArrayOf(0.2f, 0.1f), floatArrayOf(0.5f, 0.8f)),
                listOf(floatArrayOf(0.3f, 0.6f), floatArrayOf(0.4f, 0.2f))
        ) - 0.3352) shouldBeLessOrEqualTo 0.00001
    }
}
