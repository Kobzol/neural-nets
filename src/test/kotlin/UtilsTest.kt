import nn.shuffleMultiple
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class UtilsTest
{
    @Test
    fun `shuffleMultiple shuffles arrays together`()
    {
        val list = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        val second = list.toMutableList()

        shuffleMultiple(list, second)

        list shouldEqual second
    }
}
