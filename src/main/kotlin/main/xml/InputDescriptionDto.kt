package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false)
class InputDescriptionDto
{
    @field:Element(name = "minimum")
    var minimum: Double = 0.0

    @field:Element(name = "maximum")
    var maximum: Double = 0.0

    @field:Element(name = "name")
    var name: String = ""
}
