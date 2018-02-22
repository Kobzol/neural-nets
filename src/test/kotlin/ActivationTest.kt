import koma.extensions.get
import koma.matrix.ejml.EJMLMatrixFactory
import nn.activation.Activation
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

        val output = activation.forward(EJMLMatrixFactory().create(doubleArrayOf(1.0, 2.0, 3.0)))
        output[0, 0] shouldEqual 2.0f
        output[0, 1] shouldEqual 4.0f
        output[0, 2] shouldEqual 6.0f
    }
}
