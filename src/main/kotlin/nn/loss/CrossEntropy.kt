package nn.loss

class CrossEntropy : Loss
{
    override fun forward(output: Double, label: Double): Double
    {
        return label * Math.log1p(output) + (1.0 - label) * Math.log1p(1.0 - output)
    }
    override fun backward(output: Double, label: Double): Double = output - label

    override fun normalizeLoss(loss: Double, batchSize: Int): Double
    {
        if (batchSize == 0) return 0.0

        return -loss / batchSize
    }
}
