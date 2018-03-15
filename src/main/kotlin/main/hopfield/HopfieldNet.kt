package main.hopfield

import koma.extensions.get
import koma.extensions.set
import koma.eye
import koma.matrix.Matrix
import koma.matrix.ejml.EJMLMatrixFactory
import nn.DataVector
import java.util.*

class HopfieldNet(val count: Int)
{
    var weights = EJMLMatrixFactory().zeros(count, count) as Matrix<Double>

    fun train(samples: List<DataVector>)
    {
        for (sample in samples)
        {
            this.weights += sample.transpose() * sample
        }
        this.weights -= eye(this.count, this.count)
    }

    fun repair(damaged: DataVector): DataVector
    {
        val repaired = damaged.copy()
        val random = Random()

        for (i in 0 until 1000)
        {
            val neuron = random.nextInt(this.count)
            val result = repaired * this.weights.getRow(neuron).transpose()
            val activated = if (result[0] < 0) -1.0 else 1.0
            repaired[neuron] = activated
        }

        return repaired
    }
}
