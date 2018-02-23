package nn.loss

interface Loss
{
    fun forward(output: Double, label: Double): Double
    fun backward(output: Double, label: Double): Double
    fun normalizeLoss(loss: Double, batchSize: Int): Double
}
