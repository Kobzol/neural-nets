package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false)
class BackpropElementDto
{
    @field:Element(name = "inputs")
    lateinit var inputs: InputDto

    @field:Element(name = "outputs", required = false)
    var outputs: InputDto = InputDto()
}
