package main.xml

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class DataSetDto
{
    @field:ElementList(entry = "element", empty = false, required = false, inline = true)
    lateinit var elements: List<ElementDto>
}
