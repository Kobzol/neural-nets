package main.qlearning

import algorithm.util.sample
import javafx.geometry.Point2D
import koma.create
import koma.extensions.get
import koma.extensions.set
import nn.DataVector
import nn.Net
import nn.NetBuilder
import nn.activation.Linear
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.BackpropLearner
import nn.learn.SGDLearner
import nn.persistence.Persister
import java.nio.file.Paths
import java.util.*

enum class FieldType
{
    Empty,
    Wall,
    Trap,
    Goal
}
class Game(var field: Array<FieldType>, var player: Point2D)
{
    val size = Math.sqrt(field.size.toDouble()).toInt()

    fun getField(x: Int, y: Int): FieldType
    {
        return this.field[y * this.size + x]
    }
    fun getPlayerField(): FieldType
    {
        return this.getField(this.player.x.toInt(), this.player.y.toInt())
    }

    fun isFinal(): Boolean
    {
        return this.getPlayerField() == FieldType.Goal ||
                this.getPlayerField() == FieldType.Trap
    }
    fun isPossible(): Boolean
    {
        return this.isPossible(this.player)
    }
    fun isPossible(player: Point2D): Boolean
    {
        return player.x >= 0.0 && player.x < this.size &&
                player.y >= 0.0 && player.y < this.size &&
                (this.getField(player.x.toInt(), player.y.toInt()) != FieldType.Wall)
    }

    fun hash(): String
    {
        return this.field.map {
            when (it) {
                FieldType.Empty -> ' '
                FieldType.Wall -> 'W'
                FieldType.Trap -> 'T'
                FieldType.Goal -> 'G'
            }
        }.joinToString(",") + this.player.x.toString() + "x" + this.player.y.toString()
    }

    fun reward(): Int
    {
        val field = this.getPlayerField()
        if (field == FieldType.Goal) return 10
        else if (field == FieldType.Trap) return -10
        return -1
    }

    fun print()
    {
        println(this.printToString())
    }
    fun printToString(): String
    {
        val buffer = StringBuffer()
        for (i in 0 until this.size)
        {
            for (j in 0 until this.size)
            {
                if (j == this.player.x.toInt() && i == this.player.y.toInt())
                {
                    buffer.append('P')
                }
                else
                {
                    val field = this.getField(j, i)
                    buffer.append(when (field) {
                        FieldType.Empty -> '_'
                        FieldType.Wall -> 'W'
                        FieldType.Goal -> '+'
                        FieldType.Trap -> 'T'
                    })
                }
                buffer.append(' ')
            }
            buffer.append('\n')
        }

        return buffer.toString()
    }

    fun encode(): DataVector
    {
        val array = DoubleArray(this.size * this.size * 4)
        var index = 0
        for (i in 0 until this.size)
        {
            for (j in 0 until this.size)
            {
                val field = this.getField(j, i)
                array[index++] = if (j == this.player.x.toInt() && i == this.player.y.toInt()) 1.0 else 0.0
                array[index++] = if (field == FieldType.Wall) 1.0 else 0.0
                array[index++] = if (field == FieldType.Trap) 1.0 else 0.0
                array[index++] = if (field == FieldType.Goal) 1.0 else 0.0
            }
        }

        return create(array)
    }
    fun copy(): Game
    {
        return Game(this.field.clone(), Point2D(this.player.x, this.player.y))
    }

    fun makeMove(action: Int): Game
    {
        val move = actions[action]
        val newPos = Point2D(this.player.x + move[0], this.player.y + move[1])

        return Game(this.field.clone(), newPos)
    }
    fun canMakeMove(action: Int): Boolean
    {
        val move = actions[action]
        val newPos = Point2D(this.player.x + move[0], this.player.y + move[1])
        return this.isPossible(newPos)
    }
}
class QTable<T>
{
    val data = mutableMapOf<T, Array<Double>>()

    fun has(state: T): Boolean
    {
        return this.data.containsKey(state)
    }
    fun get(state: T, action: Int): Double
    {
        this.init(state)
        return this.data[state]!![action]
    }
    fun getMax(state: T): Pair<Int, Double>
    {
        this.init(state)
        var maxIndex = 0
        var maxValue = this.get(state, 0)

        for (i in 1 until 4)
        {
            val value = this.get(state, i)
            if (value > maxValue)
            {
                maxValue = value
                maxIndex = i
            }
        }

        return Pair(maxIndex, maxValue)
    }
    fun set(state: T, action: Int, reward: Double)
    {
        this.init(state)
        this.data[state]!![action] = reward
    }

    private fun init(state: T)
    {
        if (!this.has(state))
        {
            this.data[state] = Array(4, { 0.0 })
        }
    }
}
data class ReplayRecord(val state: Game, val action: Int,
                        val nextState: Game, val reward: Double)

val actions = arrayOf(
        arrayOf(1, 0),
        arrayOf(0, 1),
        arrayOf(-1, 0),
        arrayOf(0, -1)
)

fun initField(size: Int, fixed: Boolean = true): Array<FieldType>
{
    val array = Array(size * size, { FieldType.Empty })
    val possibleLocations = (0 until size * size).toMutableList()
    possibleLocations.shuffle()

    if (fixed)
    {
        possibleLocations[0] = 5
        possibleLocations[1] = 10
        possibleLocations[2] = 14
    }

    array[possibleLocations[0]] = FieldType.Wall
    array[possibleLocations[1]] = FieldType.Trap
    array[possibleLocations[2]] = FieldType.Goal
    return array
}

fun createGame(fixed: Boolean = true, size: Int = 6): Game
{
    val random = Random()
    while (true)
    {
        val game = Game(initField(size, fixed), Point2D(random.nextInt(size).toDouble(),
                random.nextInt(size).toDouble()))
        if (game.isPossible() && !game.isFinal())
        {
            return game
        }
    }
}
fun playGame(q: QTable<String>, game: Game)
{
    var state = game
    var index = 0
    val repr = state.printToString().toCharArray()

    while (state.isPossible() && !state.isFinal())
    {
        val s = state.printToString()
        val p = s.indexOf('P')
        repr[p] = index.toString()[0]
        index++
        val hash = state.hash()
        val action = q.getMax(hash).first
        state = state.makeMove(action)
    }

    repr[state.printToString().indexOf('P')] = index.toString()[0]
    println(String(repr))
}
fun playGame(net: Net, game: Game)
{
    var state = game
    var index = 0
    val repr = state.printToString().toCharArray()

    while (state.isPossible() && !state.isFinal())
    {
        val s = state.printToString()
        val p = s.indexOf('P')
        repr[p] = index.toString()[0]
        index++
        val action = net.forward(state.encode()).argMax()
        state = state.makeMove(action)
    }

    repr[state.printToString().indexOf('P')] = index.toString()[0]
    println(String(repr))
}

fun trainTable(epochs: Int, learningRate: Double, q: QTable<String>, size: Int)
{
    val random = Random()

    var epsilon = 1.0

    for (i in 0 until epochs)
    {
        var game = createGame(size=size)
        while (!game.isFinal())
        {
            val hash = game.hash()
            var action = random.nextInt(actions.size)
            if (random.nextDouble() >= epsilon) // use Q-function
            {
                action = q.getMax(hash).first
            }

            var nextState = game.makeMove(action)
            if (!nextState.isPossible())
            {
                nextState = game
            }

            val reward = if (nextState == game) -1000 else nextState.reward()
            val update = q.getMax(nextState.hash()).second
            q.set(hash, action, reward + learningRate * update)

            game = nextState
        }

        if (epsilon > 0.1)
        {
            epsilon -= (1.0 / epochs)
        }
    }
}
fun trainNet(epochs: Int, learningRate: Double, size: Int): Net
{
    val random = Random()
    var epsilon = 1.0

    val net = NetBuilder()
            .add { s -> Perceptron(s, 164, Sigmoid(), createNormalInitializer(scaleToSize = true)) }
            .add { s -> Perceptron(s, 150, Sigmoid(), createNormalInitializer(scaleToSize = true)) }
            .add { s -> Perceptron(s, 4, Linear(), createNormalInitializer(scaleToSize = true)) }
            .build(size * size * 4)
    val learner = BackpropLearner(net, learningRate)
    val gamma = 0.9

    for (i in 0 until epochs)
    {
        var game = createGame(size=size)
        while (!game.isFinal())
        {
            var action = random.nextInt(actions.size)
            val prediction = net.forward(game.encode())
            if (random.nextDouble() >= epsilon) // use Q-function
            {
                action = prediction.argMax()
            }

            var nextState = game.makeMove(action)
            if (!nextState.isPossible())
            {
                nextState = game
            }

            val reward = /*if (nextState == game) -1000.0 else*/ nextState.reward().toDouble()
            val nextPrediction = net.forward(nextState.encode())
            val update = nextPrediction.max()
            val label = prediction.copy()

            label[action] = if (nextState.isFinal()) {
                reward
            } else reward + (gamma * update)

            learner.learnBatch(listOf(game.encode()), listOf(label))
            if (i % 100 == 0)
            {
                val loss = net.getLoss(listOf(game.encode()), listOf(label))
                println(loss)
            }

            game = nextState
        }

        if (epsilon > 0.1)
        {
            epsilon -= (1.0 / epochs)
        }
    }

    return net
}
fun trainNetReplay(epochs: Int, learningRate: Double): Net
{
    val random = Random()
    var epsilon = 1.0

    val net = NetBuilder()
            .add { s -> Perceptron(s, 164, Sigmoid(), createNormalInitializer(scaleToSize = true)) }
            .add { s -> Perceptron(s, 150, Sigmoid(), createNormalInitializer(scaleToSize = true)) }
            .add { s -> Perceptron(s, 4, Linear(), createNormalInitializer(scaleToSize = true)) }
            .build(64)
    val learner = BackpropLearner(net, learningRate)
    val gamma = 0.9

    val bufferSize = 1
    val batchSize = 1
    val buffer = mutableListOf<ReplayRecord>()
    var bufferIndex = 0

    for (i in 0 until epochs)
    {
        var game = createGame()
        while (!game.isFinal())
        {
            var action = random.nextInt(actions.size)
            if (random.nextDouble() >= epsilon) // use Q-function
            {
                action = net.forward(game.encode()).argMax()
            }

            var nextState = game.makeMove(action)
            if (!nextState.isPossible())
            {
                nextState = game
            }

            val reward = /*if (game == nextState) -1000.0 else*/ nextState.reward().toDouble()
            val replay = ReplayRecord(game.copy(), action, nextState.copy(), reward)

            if (buffer.size < bufferSize)
            {
                buffer += replay
            }
            else
            {
                bufferIndex = (bufferIndex + 1) % bufferSize
                buffer[bufferIndex] = replay

                val batch = sample(buffer, batchSize, random)
                val inputs = mutableListOf<DataVector>()
                val outputs = mutableListOf<DataVector>()

                for (record in batch)
                {
                    val predicted = net.forward(record.state.encode())
                    val nextPrediction = net.forward(record.nextState.encode())
                    val update = nextPrediction.max()
                    val label = predicted.copy()

                    label[action] = if (record.nextState.isFinal()) {
                        record.reward
                    } else record.reward + (gamma * update)

                    inputs.add(record.state.encode())
                    outputs.add(label)
                }

                learner.learnBatch(inputs, outputs)

                if (i % 100 == 0)
                {
                    val loss = net.getLoss(inputs, outputs)
                    println(loss)
                }
            }

            game = nextState
        }

        if (epsilon > 0.1)
        {
            epsilon -= (1.0 / epochs)
        }
    }

    return net
}

fun main(args: Array<String>)
{
    val epochs = 5000
    val learningRate = 0.02
    val size = 6

    val brain = trainNet(epochs, learningRate, size)
    Persister().persistNet(brain, Paths.get("qlearning.json"))
    //val brain = Persister().loadNet(Paths.get("qlearning.json"))

    /*val brain = QTable<String>()
    trainTable(epochs, learningRate, brain, size)*/

    println("Trained")

    while (true)
    {
        val game = createGame(size=size)
        game.print()

        val points = readLine()!!.split(" ").map { it.toInt() }.toIntArray()
        val player = Point2D(points[1].toDouble(), points[0].toDouble())
        playGame(brain, Game(game.field.clone(), player))
    }
}
