import nn.createHebbNet
import nn.learn.HebbLearner
import nn.toVec
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.junit.jupiter.api.Test

class HebbTest
{
    @Test
    fun `Hebb learns AND`()
    {
        val net = createHebbNet(2)
        val learner = HebbLearner(net, 0.1)
        val inputs = listOf(
                floatArrayOf(0.0f, 0.0f),
                floatArrayOf(0.0f, 1.0f),
                floatArrayOf(1.0f, 0.0f),
                floatArrayOf(1.0f, 1.0f)
        ).map { toVec(it) }
        val outputs = listOf(
                floatArrayOf(0.0f),
                floatArrayOf(0.0f),
                floatArrayOf(0.0f),
                floatArrayOf(1.0f)
        ).map { toVec(it) }

        for (i in 0 until 20)
        {
            learner.learnBatch(inputs, outputs)
        }

        net.getLoss(inputs, outputs) shouldEqual 0.0f
    }

    @Test
    fun `Hebb learns OR`()
    {
        val net = createHebbNet(2)
        val learner = HebbLearner(net, 0.1)
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
                floatArrayOf(1.0f)
        ).map { toVec(it) }

        for (i in 0 until 20)
        {
            learner.learnBatch(inputs, outputs)
        }

        net.getLoss(inputs, outputs) shouldEqual 0.0f
    }

    @Test
    fun `Hebb doesn't learn XOR`()
    {
        val net = createHebbNet(2)
        val learner = HebbLearner(net, 0.05)
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

        for (i in 0 until 500)
        {
            learner.learnBatch(inputs, outputs)
        }

        net.getLoss(inputs, outputs) shouldNotEqual 0.0f
    }
}
