package nn

import nn.layer.Layer

class Net(val layers: List<Layer>)
{
    public fun feedForward(inputs: FloatArray): FloatArray
    {
        var data = inputs
        for (layer in this.layers)
        {
            layer.forward(data)
            data = layer.outputs
        }

        return data
    }
}
