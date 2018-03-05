package nn

import koma.matrix.ejml.EJMLMatrixFactory
import java.util.*
import kotlin.system.measureNanoTime

fun toVec(array: FloatArray): DataVector
{
    return EJMLMatrixFactory().create(array.map { it.toDouble() }.toDoubleArray())
}
fun toVec(array: DoubleArray): DataVector
{
    return EJMLMatrixFactory().create(array)
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
    return (0..(data.size - 1) step size).map { data.subList(it, Math.min(data.size, it + size)) }
}

inline fun profile(name: String, cb: () -> Unit)
{
    val time = measureNanoTime(cb)
    println("$name: ${time / 1_000_000} ms")
}
