package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false)
class InputDescriptionDto
{
    @field:Element(name = "minimum")
    var minimum: Float = 0.0f

    @field:Element(name = "maximum")
    var maximum: Float = 0.0f

    @field:Element(name = "name")
    var name: String = ""
}
