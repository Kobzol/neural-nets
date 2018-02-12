package main.hebb

import javafx.application.Application
import javafx.scene.Group
import javafx.stage.Stage
import javafx.scene.Scene

class HebbApp: Application()
{
    private val screen: MainScreen = MainScreen()

    override fun start(stage: Stage)
    {
        val root = Group()
        stage.scene = Scene(root)
        stage.title = "Hebb perceptron"
        root.children.add(this.screen)

        stage.show()
    }
}

fun main(args: Array<String>)
{
    Application.launch(HebbApp::class.java, *args)
}
