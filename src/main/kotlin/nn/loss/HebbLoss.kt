package nn.loss

class HebbLoss : Loss
{
    override fun forward(output: Double, label: Double): Double
    {
        return label - output
    }
    override fun backward(output: Double, label: Double): Double = output - label

    override fun normalizeLoss(loss: Double, batchSize: Int): Double = loss
}
