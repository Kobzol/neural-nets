import nn.NetBuilder
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.BackpropLearner
import nn.toVec
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class BackpropTest
{
    @Test
    fun `Backprop learns XOR`()
    {
        val builder = NetBuilder()
        val net = builder
                .add { s -> Perceptron(s, 10, Sigmoid(), createNormalInitializer()) }
                .add { s -> Perceptron(s, 1, Sigmoid(), createNormalInitializer()) }
                .build(2)
        val learner = BackpropLearner(net, 0.01f)
        val inputs = listOf(
                floatArrayOf(0.0f, 0.0f),
                floatArrayOf(0.0f, 1.0f),
                floatArrayOf(1.0f, 0.0f),
                floatArrayOf(1.0f, 1.0f)
        ).map { toVec(it) }
        val outputs = listOf(
                floatArrayOf(0.0f),
                floatArrayOf(1.0f),
                floatArrayOf(1.0f),
                floatArrayOf(0.0f)
        ).map { toVec(it) }

        for (i in 0 until 50000)
        {
            learner.learnBatch(inputs, outputs)
        }

        net.getLoss(inputs, outputs) shouldEqual 0.0f
    }
}
