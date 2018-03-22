package main.racer

import cz.vsb.cs.neurace.basicClient.DriverInterface
import java.util.HashMap

data class DriveResult(val wheel: Double, val acceleration: Double)
data class DriveInput(val checkpoint: Double, val speed: Double, val angle: Double, val frontSensors: DoubleArray,
                      val rearSensors: DoubleArray, val distances: DoubleArray, val skid: Double, val friction: Double)
{
    fun linearize(): DoubleArray
    {
        return doubleArrayOf(this.checkpoint, this.speed, this.angle, *this.frontSensors,
                *this.rearSensors, *this.distances, this.skid, this.friction)
    }

    fun getTrainedInput(): DoubleArray
    {
        return this.linearize()
    }

    companion object
    {
        fun deserialize(data: DoubleArray): DriveInput
        {
            return DriveInput(
                    data[0],
                    data[1],
                    data[2],
                    data.sliceArray(3..14),
                    data.sliceArray(15..18),
                    data.sliceArray(19..23),
                    data[24],
                    data[25]
            )
        }
    }
}

abstract class Driver: DriverInterface
{
    override fun drive(values: HashMap<String, Float>): HashMap<String, Float>
    {
        val checkpoint = values["checkpoint"]!!.toDouble()
        val speed = values["speed"]!!.toDouble()
        val angle = values["angle"]!!.toDouble()
        val frontSensors = arrayOf(
                "sensorLeft1",
                "sensorLeft2",
                "sensorFrontLeft",
                "sensorFrontLeftCorner1",
                "sensorFrontLeftCorner2",
                "sensorFrontMiddleLeft",
                "sensorFrontMiddleRight",
                "sensorFrontRightCorner1",
                "sensorFrontRightCorner2",
                "sensorFrontRight",
                "sensorRight2",
                "sensorRight1"
        ).map { values[it]!!.toDouble() }
        .toDoubleArray()

        val rearSensors = arrayOf(
                "sensorRearLeftCorner1",
                "sensorRearLeftCorner2",
                "sensorRearRightCorner1",
                "sensorRearRightCorner2"
        ).map { values[it]!!.toDouble() }
        .toDoubleArray()

        val distances = arrayOf(
                "distance0",
                "distance4",
                "distance8",
                "distance16",
                "distance32"
        ).map { values[it]!!.toDouble() }
        .toDoubleArray()
        val skid = values["skid"]!!.toDouble()
        val friction = values["friction"]!!.toDouble()

        val input = DriveInput(checkpoint, speed, angle, frontSensors, rearSensors, distances, skid, friction)
        val result = this.drive(input)

        return hashMapOf(
                Pair("wheel", result.wheel.toFloat()),
                Pair("acc", result.acceleration.toFloat())
        )
    }

    abstract fun drive(input: DriveInput): DriveResult
}
