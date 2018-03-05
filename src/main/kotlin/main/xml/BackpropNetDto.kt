package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(strict = false)
class BackpropNetDto
{
    @field:Element(name = "inputDescriptions")
    lateinit var data: BackpropPerceptronDto

    @field:Element(name = "neuronInLayersCount")
    lateinit var layers: NeuronLayerDto

    @field:Element(name = "testSet")
    lateinit var testSet: BackpropDataSetDto

    @field:Element(name = "trainSet")
    lateinit var trainSet: BackpropDataSetDto
}
