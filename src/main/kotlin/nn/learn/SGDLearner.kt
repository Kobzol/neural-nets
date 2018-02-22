package nn.learn

import nn.DataVector
import nn.Net
import nn.partition
import nn.shuffleMultiple

class SGDLearner(net: Net,
                 private var learningRate: Float,
                 private val miniBatchSize: Int)
{
    private val learner = BackpropLearner(net, learningRate / miniBatchSize)

    fun learnBatch(inputs: List<DataVector>, labels: List<DataVector>)
    {
        val features = inputs.toMutableList()
        val outputs = labels.toMutableList()
        //shuffleMultiple(features, outputs)

        val miniBatchFeatures = partition(features, this.miniBatchSize)
        val miniBatchLabels = partition(outputs, this.miniBatchSize)

        for (batch in miniBatchFeatures.indices)
        {
            val ins = miniBatchFeatures[batch]
            val outs = miniBatchLabels[batch]

            this.learner.learnBatch(ins, outs)
        }
    }
}
