package main.neuroevolution

import algorithm.Bounds
import algorithm.FitnessEvaluator
import algorithm.PopulationGenerator
import algorithm.evolution.crossover.DECrossover
import algorithm.evolution.de.DE
import algorithm.evolution.mutation.DERand1
import koma.extensions.set
import nn.NetBuilder
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.toVec

fun main(args: Array<String>)
{
    val net = NetBuilder()
            .add { s -> Perceptron(s, 2, Sigmoid(), createNormalInitializer()) }
            .add { s -> Perceptron(s, 1, Sigmoid(), createNormalInitializer()) }
            .build(2)
    val inputs = listOf(
            floatArrayOf(0.0f, 0.0f),
            floatArrayOf(0.0f, 1.0f),
            floatArrayOf(1.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f)
    ).map { toVec(it) }
    val outputs = listOf(
            floatArrayOf(0.0f),
            floatArrayOf(1.0f),
            floatArrayOf(1.0f),
            floatArrayOf(0.0f)
    ).map { toVec(it) }

    val bounds = Bounds(-1.0f, 1.0f)
    val boundsArray = arrayOf(
            bounds,
            bounds,
            bounds,
            bounds,
            bounds,
            bounds,
            bounds,
            bounds,
            bounds
    )

    var best = 1.0
    val evaluator =  object: FitnessEvaluator {
        override fun evaluate(data: FloatArray): Float
        {
            net.layers[0].biases[0] = data[0].toDouble()
            net.layers[0].weights[0, 0] = data[1].toDouble()
            net.layers[0].weights[0, 1] = data[2].toDouble()
            net.layers[0].biases[1] = data[3].toDouble()
            net.layers[0].weights[1, 0] = data[4].toDouble()
            net.layers[0].weights[1, 1] = data[5].toDouble()
            net.layers[1].biases[0] = data[6].toDouble()
            net.layers[1].weights[0, 0] = data[7].toDouble()
            net.layers[1].weights[0, 1] = data[8].toDouble()

            val loss = net.getLoss(inputs, outputs)
            best = Math.min(best, loss)
            println(loss)

            return (1.0 - loss).toFloat()
        }
    }
    val de = DE(PopulationGenerator().generateAreaPopulation(100, boundsArray),
            DECrossover(), 0.5f,
            DERand1(boundsArray), 0.2f,
            boundsArray, evaluator
    )


    for (i in 0 until 5000)
    {
        de.runIteration()
    }

    println(best)
}
