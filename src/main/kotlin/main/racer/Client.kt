package main.racer

import cz.vsb.cs.neurace.basicClient.RaceConnector
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import koma.create
import koma.extensions.map
import koma.matrix.ejml.EJMLMatrixFactory
import nn.Net
import nn.NetBuilder
import nn.activation.Linear
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import nn.learn.BackpropLearner
import nn.learn.SGDLearner
import nn.persistence.Persister
import nn.toVec
import java.io.File
import java.nio.file.Paths
import java.util.*


class DriverApp: Application()
{
    private val driver = KeyboardDriver()
    private lateinit var netDriver: NetDriver
    private lateinit var raceConnector: RaceConnector

    override fun start(stage: Stage)
    {
        val keyboard = this.parameters.raw[0] == "keyboard"

        val root = Group()
        stage.scene = Scene(root)
        stage.title = "Driver"
        stage.width = 100.0
        stage.height = 100.0
        stage.scene.addEventHandler(KeyEvent.KEY_PRESSED, { key ->
            when {
                key.code === KeyCode.LEFT -> this.driver.changeKeymap(this.driver.keymap.clone(left=true))
                key.code === KeyCode.RIGHT -> this.driver.changeKeymap(this.driver.keymap.clone(right=true))
                key.code === KeyCode.UP -> this.driver.changeKeymap(this.driver.keymap.clone(up=true))
                key.code === KeyCode.DOWN -> this.driver.changeKeymap(this.driver.keymap.clone(down=true))
                key.code === KeyCode.SPACE -> this.driver.toggleCollection()
            }
        })
        stage.scene.addEventHandler(KeyEvent.KEY_RELEASED, { key ->
            when {
                key.code === KeyCode.LEFT -> this.driver.changeKeymap(this.driver.keymap.clone(left=false))
                key.code === KeyCode.RIGHT -> this.driver.changeKeymap(this.driver.keymap.clone(right=false))
                key.code === KeyCode.UP -> this.driver.changeKeymap(this.driver.keymap.clone(up=false))
                key.code === KeyCode.DOWN -> this.driver.changeKeymap(this.driver.keymap.clone(down=false))
            }
        })

        if (!keyboard)
        {
            netDriver = NetDriver(Persister().loadNet(Paths.get(this.parameters.raw[1])))
        }

        val thread = object: Thread() {
            override fun run() {
                val host = "localhost"
                val port = 9461
                var raceName = "test"
                var driverName = "basic_client" + Random().nextInt(1000).toString()
                var carType: String? = null
                // kontrola argumentu programu
                raceConnector = RaceConnector(host, port, null)
                System.err.println("argumenty: server port nazev_zavodu jmeno_ridice [typ_auta]")
                val raceList = raceConnector.listRaces()
                raceName = raceList[Random().nextInt(raceList.size)]
                val carList = raceConnector.listCars(raceName)
                carType = carList[2]//Random().nextInt(carList.size)]
                driverName += "_" + carType!!

                raceConnector.driver = if (keyboard) driver else netDriver
                raceConnector.start(raceName, driverName, carType)
            }
        }
        thread.start()

        stage.setOnCloseRequest {
            if (keyboard)
            {
                synchronized(this.driver.mutex, {
                    SamplePersister().persistSamples(this.driver.samples, Paths.get(this.findFileName()))
                })
            }
            raceConnector.stop()
        }

        stage.show()
    }

    private fun findFileName(): String
    {
        var index = 0
        while (true)
        {
            val name = "samples$index.json"
            if (!File(name).exists()) return name

            index++
        }
    }
}

fun train(samplePath: String, netPath: String? = null): Net
{
    val samples = SamplePersister().loadSamples(Paths.get("$samplePath.json"))

    val rawInputs = samples.map {
        DriveInput.deserialize(it.input).getTrainedInput()
    }.toList()
    val rawOutputs = samples.map { it.output }.toList()

    val validIndices = mutableListOf<Int>()
    val map = mutableSetOf<String>()
    for (i in rawInputs.indices)
    {
        val hash = rawInputs[i].joinToString(".") { it.toString() }
        if (!map.contains(hash))
        {
            map += hash
            validIndices += i
        }
    }

    val inputs = validIndices.map { toVec(rawInputs[it]) }
    val outputs = validIndices.map { toVec(rawOutputs[it]) }

    val net = NetBuilder()
            .add { s -> Perceptron(s, 40, Sigmoid(), createNormalInitializer(scaleToSize = false)) }
            .add { s -> Perceptron(s, samples[0].output.size, Sigmoid(), createNormalInitializer(scaleToSize = false)) }
            .build(inputs[0].numCols())
    val learner = BackpropLearner(net, 0.01)

    for (i in 0 until 1000)
    {
        learner.learnBatch(inputs, outputs)
        println("Loss: ${net.getLoss(inputs, outputs)}")
    }

    if (netPath != null)
    {
        Persister().persistNet(net, Paths.get("$netPath.json"))
    }

    return net
}

fun main(args: Array<String>)
{
    if (args[0] == "train")
    {
        train("samples4", "net-driver")
    }
    else Application.launch(DriverApp::class.java, *args)
}
