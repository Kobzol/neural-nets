package main.hebb

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
import main.xml.NetXmlParser
import java.nio.file.Paths

class HebbApp: Application()
{
    private val screen: MainScreen = MainScreen(NetXmlParser()
            .parsePerceptronTask(Paths.get("xml/obdelnik_rozsah.xml")))

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
