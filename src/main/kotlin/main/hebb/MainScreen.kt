package main.hebb

import javafx.scene.control.Button
import javafx.scene.layout.VBox
import nn.Net
import nn.NetBuilder
import nn.activation.Signum
import nn.layer.MLP
import nn.learn.HebbLearner

class MainScreen : VBox()
{
    val canvas = PointCanvas(300.0, 300.0)
    var net: Net
    var learner: HebbLearner

    init
    {
        val builder = NetBuilder()
        builder.add { s -> MLP(s, 2, Signum()) }
        this.net = builder.build(2)
        this.learner = HebbLearner(this.net, 0.1f)

        val learnBtn = Button("Learn one step")
        learnBtn.setOnAction {

        }

        this.children.add(learnBtn)
        this.children.add(this.canvas)
    }
}
