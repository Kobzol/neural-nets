package nn

import koma.create
import koma.matrix.MatrixTypes
import java.util.*

fun toVec(array: FloatArray): DataVector
{
    return create(array.map { it.toDouble() }.toDoubleArray(), MatrixTypes.FloatType)
}

fun <T> shuffleMultiple(vararg lists: MutableList<T>)
{
    val random = Random()
    var size = lists[0].size
    for (i in lists[0].indices)
    {
        val index = random.nextInt(size)
        for (list in lists)
        {
            val tmp = list[index]
            list[index] = list[size - 1]
            list[size - 1] = tmp
        }

        size--
    }
}

fun <T> partition(data: List<T>, size: Int): List<List<T>>
{
    return (0..(data.size - 1) step size).map { data.subList(it, it + size) }
}
