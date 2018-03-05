package main.xml

import nn.DataVector
import nn.toVec
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class BackpropDataSetDto
{
    @field:ElementList(entry = "testSetElement", empty = false, required = false, inline = true)
    lateinit var testElements: List<BackpropElementDto>

    @field:ElementList(entry = "trainSetElement", empty = false, required = false, inline = true)
    lateinit var trainElements: List<BackpropElementDto>

    fun inputs(): List<DataVector>
    {
        return this.getElements().map { toVec(it.inputs.values.toDoubleArray()) }
    }
    fun outputs(): List<DataVector>
    {
        return this.getElements().map { toVec(it.outputs.values.toDoubleArray()) }
    }

    private fun getElements(): List<BackpropElementDto>
    {
        if (this.trainElements.isNotEmpty()) return this.trainElements
        return this.testElements
    }
}
