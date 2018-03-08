package main.qlearning

import javafx.geometry.Point2D
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
class Matrix<T>
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

fun createGame(): Game
{
    val random = Random()
    while (true)
    {
        val game = Game(initField(4), Point2D(random.nextInt(4).toDouble(), random.nextInt(4).toDouble()))
        if (game.isPossible() && !game.isFinal())
        {
            return game
        }
    }
}
fun playGame(q: Matrix<String>, game: Game)
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

fun main(args: Array<String>)
{
    val epochs = 10000
    var epsilon = 1.0
    val learningRate = 0.2
    val random = Random()
    val q = Matrix<String>()

    for (i in 0 until epochs)
    {
        var game = createGame()
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

    println("Trained")

    while (true)
    {
        val game = Game(initField(4), Point2D(-1.0, -1.0))
        game.print()

        val points = readLine()!!.split(" ").map { it.toInt() }.toIntArray()
        val player = Point2D(points[1].toDouble(), points[0].toDouble())
        playGame(q, Game(game.field.clone(), player))
    }
}
