package main.xml

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class NeuronLayerDto
{
    @field:ElementList(entry = "neuronInLayerCount", empty = false, required = false, inline = true)
    lateinit var counts: List<Int>
}
