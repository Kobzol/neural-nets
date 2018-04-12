package main.racer

import algorithm.util.clamp
import koma.abs
import koma.extensions.get
import nn.Net
import nn.toVec

class NetDriver(val net: Net): Driver()
{
    private var acc = 0.5
    private val accScale = 0.05

    override fun drive(input: DriveInput): DriveResult
    {
        val output = this.net.forward(toVec(input.getTrainedInput()))
        val targetSpeed = output[1]
        val diff = abs(input.speed - targetSpeed)

        if (diff < 0.005)
        {
            this.acc = 0.5
        }
        else if (input.speed < targetSpeed)
        {
            this.acc += accScale
        }
        else this.acc -= accScale

        this.acc = clamp(this.acc, 0.0, 1.0)

        println("W: ${output[0]}, S: ${output[1]}")

        return DriveResult(output[0], this.acc)
    }
}
