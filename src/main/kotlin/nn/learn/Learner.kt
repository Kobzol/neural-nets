package nn.learn

import nn.DataVector

interface Learner
{
    var learningRate: Double
    fun learnBatch(inputs: List<DataVector>, labels: List<DataVector>)
}
