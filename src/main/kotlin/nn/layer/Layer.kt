package nn.layer

interface Layer
{
    val outputs: FloatArray
    val weights: FloatArray
    val biases: FloatArray

    fun forward(data: FloatArray)
}
