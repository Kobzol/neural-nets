package nn

import koma.extensions.get
import koma.extensions.mapIndexed
import koma.matrix.Matrix
import nn.layer.Layer
import nn.loss.Loss
import nn.loss.QuadraticLoss

typealias DataVector = Matrix<Double>

class Net(val layers: List<Layer>, val loss: Loss = QuadraticLoss())
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
        val loss = inputs.zip(labels).map { (input, label) ->
            this.forward(input)
                    .mapIndexed { _, col, ele -> this.loss.forward(ele, label[col]) }
                    .elementSum()
        }.sum()
        return this.loss.normalizeLoss(loss, inputs.size)
    }

    fun countCorrect(testInputs: List<DataVector>, testLabels: List<DataVector>): Int
    {
        return testInputs.zip(testLabels).count { (input, label) ->
            val res = this.forward(input)
            res.argMax() == label.argMax()
        }
    }
}
