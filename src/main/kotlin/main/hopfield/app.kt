package main.hopfield

import koma.create
import koma.extensions.get
import koma.sqrt

fun main(args: Array<String>)
{
    val net = HopfieldNet(16)
    val input = create(doubleArrayOf(
            -1.0, -1.0, -1.0, -1.0,
            -1.0, 1.0, 1.0, -1.0,
            -1.0, 1.0, 1.0, -1.0,
            -1.0, -1.0, -1.0, -1.0
    ))

    net.train(listOf(input))

    val damaged = create(doubleArrayOf(
            -1.0, -1.0, -1.0, -1.0,
            -1.0, -1.0, 1.0, -1.0,
            -1.0, -1.0, -1.0, -1.0,
            -1.0, -1.0, -1.0, -1.0
    ))
    val repaired = net.repair(damaged)
    for (i in 0 until 4)
    {
        for (j in 0 until 4)
        {
            print("${repaired[i * 4 + j]} ")
        }
        println()
    }
}
