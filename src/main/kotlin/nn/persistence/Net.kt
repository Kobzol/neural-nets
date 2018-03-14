package nn.persistence

class Layer
{
    lateinit var activation: String
    var size: Int = 0
    lateinit var biases: DoubleArray
    lateinit var weights: DoubleArray
}

class Net
{
    var inputSize: Int = 0
    lateinit var layers: List<Layer>
}
