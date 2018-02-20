package nn

import koma.create
import koma.matrix.MatrixTypes

fun toVec(array: FloatArray): DataVector
{
    return create(array.map { it.toDouble() }.toDoubleArray(), MatrixTypes.FloatType)
}
