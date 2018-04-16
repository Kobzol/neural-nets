package main.racer

import algorithm.util.clamp
import koma.abs
import koma.platformsupport.signum
import koma.sign
import java.util.*

class Keymap
{
    var up: Boolean = false
    var down: Boolean = false
    var left: Boolean = false
    var right: Boolean = false

    fun clone(up: Boolean? = null, down: Boolean? = null,
              left: Boolean? = null, right: Boolean? = null): Keymap
    {
        val keymap = Keymap()
        keymap.up = up ?: this.up
        keymap.down = down ?: this.down
        keymap.left = left ?: this.left
        keymap.right = right ?: this.right
        return keymap
    }
}

class KeyboardDriver: Driver()
{
    var keymap = Keymap()
    val samples: MutableList<Sample> = mutableListOf()

    val mutex = Object()

    private var wheel = 0.5
    private var acceleration = 0.5

    private var targetWheel = 0.5
    private var targetSpeed = 0.65

    private val wheelScale = 0.01
    private val wheelReturn = 0.005
    private val wheelLimit = 0.5

    private val accScale = 0.01
    private val accReturn = 0.04
    private val speedScale = 0.001

    private var collectionOn = false

    private val random = Random()

    override fun drive(input: DriveInput): DriveResult
    {
        if (input.speed < this.targetSpeed)
        {
            this.acceleration += this.accScale
        }
        else if (input.speed > this.targetSpeed)
        {
            this.acceleration -= this.accScale
        }
        this.acceleration = this.clampNear(this.acceleration, this.targetSpeed)
        this.acceleration = clamp(this.acceleration, 0.0, 1.0)

        if (this.wheel < this.targetWheel)
        {
            this.wheel += this.wheelScale
        }
        else if (this.wheel > this.targetWheel)
        {
            this.wheel -= this.wheelScale
        }
        this.wheel = this.clampNear(this.wheel, this.targetWheel)
        this.wheel = clamp(this.wheel, 0.0, 1.0)

        synchronized(this.mutex, {
            this.drive()

            if (this.collectionOn && this.random.nextDouble() < 0.2)
            {
                this.samples += Sample(input.linearize(), doubleArrayOf(this.wheel, this.targetSpeed))
                println("COLLECTED")
            }
        })

        println("W: $targetWheel, S: $targetSpeed")
        println("W: $wheel, S: $acceleration")

        return DriveResult(this.wheel, this.acceleration)
    }

    fun toggleCollection()
    {
        synchronized(this.mutex, {
            this.collectionOn = !this.collectionOn
        })
    }

    fun changeKeymap(keymap: Keymap)
    {
        synchronized(this.mutex, {
            this.keymap = keymap
        })
    }

    private fun drive()
    {
        synchronized(this.mutex, {
            if (this.keymap.left || this.keymap.right)
            {
                this.targetWheel += if (this.keymap.right) this.wheelScale else -this.wheelScale
                this.targetWheel = clamp(this.targetWheel, 0.5 - this.wheelLimit, 0.5 + this.wheelLimit)
            }
            else this.returnWheel()

            if (abs(this.targetWheel - 0.5) < 0.01)
            {
                this.targetWheel = 0.5
            }

            if (this.keymap.up || this.keymap.down)
            {
                this.targetSpeed += if (this.keymap.up) this.speedScale else -this.speedScale
                this.targetSpeed = clamp(this.targetSpeed, 0.0, 1.0)
            }
        })
    }

    private fun returnWheel()
    {
        val toMiddle = 0.5 - this.targetWheel
        this.targetWheel += signum(toMiddle) * this.wheelReturn
    }
    private fun returnAccel()
    {
        val toMiddle = 0.5 - this.acceleration
        this.acceleration += signum(toMiddle) * this.accReturn
        if (abs(this.acceleration - 0.5) < 0.005)
        {
            this.acceleration = 0.5
        }
    }
    private fun clampNear(actual: Double, target: Double): Double
    {
        if (abs(actual - target) < 0.005) return target
        return actual
    }
}
