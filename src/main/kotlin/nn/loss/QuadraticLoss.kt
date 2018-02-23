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
        return loss / (2.0 * batchSize)
    }
}
