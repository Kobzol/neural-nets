package main.xml

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false)
class PerceptronTaskDto
{
    @field:Element(name = "perceptron")
    lateinit var perceptron: PerceptronDto

    @field:Element(name = "TestSet")
    lateinit var testSet: DataSetDto

    @field:Element(name = "TrainSet")
    lateinit var trainSet: DataSetDto
}
