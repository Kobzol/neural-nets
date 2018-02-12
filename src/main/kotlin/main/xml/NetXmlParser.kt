package main.xml

import org.simpleframework.xml.core.Persister
import java.nio.file.Path

class NetXmlParser
{
    private val serializer = Persister()

    fun parsePerceptronTask(path: Path)
    {
        val data = this.serializer.read(PerceptronTaskDto::class.java, path.toFile())
    }
}
