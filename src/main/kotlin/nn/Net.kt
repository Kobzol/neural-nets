package nn

import koma.matrix.Matrix
import nn.layer.Layer

typealias DataVector = Matrix<Double>

class Net(val layers: List<Layer>)
{
    fun forward(inputs: DataVector): DataVector
    {
        var data = inputs
        for (layer in this.layers)
        {
            data = layer.activation.forward(layer.forward(data))
        }

        return data
    }

    fun getLoss(inputs: List<DataVector>, labels: List<DataVector>): Double
    {
        return inputs.zip(labels).map { (input, label) ->
            val output = this.forward(input)
            val diff = label - output
            diff.elementTimes(diff).elementSum()
        }.sum()
    }
}
