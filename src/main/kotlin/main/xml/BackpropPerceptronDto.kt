package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class BackpropPerceptronDto
{
    @field:Element(name = "learningRate", required = false)
    var learningRate: Double = 0.0

    @field:Element(name = "name", required = false)
    var name: String = ""

    @field:ElementList(entry = "inputDescription", empty = false, required = false, inline = true)
    lateinit var data: List<InputDescriptionDto>
}
