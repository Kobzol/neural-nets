package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class PerceptronDto
{
    @field:Element(name = "lerningRate")
    var learningRate: Float = 0.0f

    @field:Element(name = "name")
    var name: String = ""

    @field:ElementList(entry = "inputDescriptions", empty = false, required = false, inline = true)
    lateinit var data: List<InputDescriptionDto>
}