package main.xml

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class InputDto
{
    @field:ElementList(entry = "value", empty = false, required = false, inline = true)
    var values: MutableList<Double> = mutableListOf()
}
