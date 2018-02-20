import nn.Net
import nn.activation.Activation
import nn.activation.Sigmoid
import nn.layer.Perceptron
import nn.toVec
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
                listOf(toVec(floatArrayOf(0.2f, 0.1f)), toVec(floatArrayOf(0.5f, 0.8f))),
                listOf(toVec(floatArrayOf(0.3f, 0.6f)), toVec(floatArrayOf(0.4f, 0.2f)))
        ) - 0.573f) shouldBeLessOrEqualTo 0.00001f
    }
}
