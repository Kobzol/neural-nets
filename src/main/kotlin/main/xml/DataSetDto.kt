package main.xml

import nn.DataVector
import nn.toVec
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class DataSetDto
{
    @field:ElementList(entry = "element", empty = false, required = false, inline = true)
    lateinit var elements: List<ElementDto>

    fun inputs(): List<DataVector>
    {
        return this.elements.map { toVec(it.inputs.values.toFloatArray()) }
    }
    fun outputs(): List<DataVector>
    {
        return this.elements.map { toVec(floatArrayOf(it.output)) }
    }
}
