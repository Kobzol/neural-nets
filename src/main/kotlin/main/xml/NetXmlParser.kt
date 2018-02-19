package main.xml

import org.simpleframework.xml.core.Persister
import java.nio.file.Path

class NetXmlParser
{
    private val serializer = Persister()

    fun parsePerceptronTask(path: Path): PerceptronTaskDto
    {
        return this.serializer.read(PerceptronTaskDto::class.java, path.toFile())
    }
}
