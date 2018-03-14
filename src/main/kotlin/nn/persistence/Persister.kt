package nn.persistence

import com.google.gson.Gson
import koma.create
import nn.Net
import nn.NetBuilder
import nn.activation.Activation
import nn.activation.Linear
import nn.activation.ReLu
import nn.activation.Sigmoid
import nn.createNormalInitializer
import nn.layer.Perceptron
import java.io.FileReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

class Persister
{
    fun persistNet(net: Net, path: Path)
    {
        val persisted = nn.persistence.Net()
        persisted.inputSize = net.layers[0].inputSize
        persisted.layers = net.layers.map {
            val layer = Layer()
            layer.size = it.neuronCount
            layer.biases = it.biases.getDoubleData()
            layer.weights = it.weights.getDoubleData()
            layer.activation = this.serializeActivation(it.activation)
            layer
        }
        val json = Gson().toJson(persisted)
        Files.write(path, json.toByteArray(Charset.forName("UTF-8")))
    }

    fun loadNet(path: Path): Net
    {
        val net = Gson().fromJson(String(Files.readAllBytes(path), Charset.forName("UTF-8")),
                nn.persistence.Net::class.java)
        val builder = NetBuilder()
        net.layers.forEach { it ->
            builder.add { s ->
                val perceptron = Perceptron(s, it.size, this.deserializeActivation(it.activation), createNormalInitializer())
                perceptron.biases = create(it.biases)
                perceptron.weights = create(it.weights, it.size, s)
                perceptron
            }
        }
        return builder.build(net.inputSize)
    }

    private fun serializeActivation(activation: Activation): String
    {
        if (activation is Sigmoid) return "Sigmoid"
        if (activation is ReLu) return "ReLu"
        if (activation is Linear) return "Linear"
        return "Sigmoid"
    }
    private fun deserializeActivation(activation: String): Activation
    {
        if (activation == "Sigmoid") return Sigmoid()
        if (activation == "ReLu") return ReLu()
        if (activation == "Linear") return Linear()
        return Sigmoid()
    }
}
