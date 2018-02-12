package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false)
class ElementDto
{
    @field:Element(name = "inputs")
    lateinit var inputs: InputDto

    @field:Element(name = "output", required = false)
    var output: Float = 0.0f
}
