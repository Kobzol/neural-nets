package nn.math

fun dot(x0: FloatArray, x1: FloatArray): Float
{
    return x0.zip(x1).map { it.first * it.second }.sum()
}
fun sum(x0: FloatArray, x1: FloatArray): FloatArray
{
    return x0.zip(x1).map { it.first + it.second }.toFloatArray()
}
