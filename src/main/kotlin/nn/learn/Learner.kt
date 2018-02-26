package nn.learn

import nn.DataVector

interface Learner
{
    fun learnBatch(inputs: List<DataVector>, labels: List<DataVector>)
}
