package nn.loss

class QuadraticLoss: Loss
{
    override fun forward(output: Double, label: Double): Double
    {
        return Math.pow(output - label, 2.0)
    }
    override fun backward(output: Double, label: Double): Double = output - label

    override fun normalizeLoss(loss: Double, batchSize: Int): Double
    {
        if (batchSize == 0) return 0.0
        return loss / (2.0 * batchSize)
    }
}
