package main.backprop

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
import main.xml.NetXmlParser
import java.nio.file.Paths

class BackpropApp: Application()
{
    private val screen: MainScreen = MainScreen(NetXmlParser()
            .parseBackpropNet(Paths.get("xml/example2D_1.xml")))

    override fun start(stage: Stage)
    {
        val root = Group()
        stage.scene = Scene(root)
        stage.title = "Backpropagation"
        root.children.add(this.screen)

        stage.show()
    }
}

fun main(args: Array<String>)
{
    Application.launch(BackpropApp::class.java, *args)
}
