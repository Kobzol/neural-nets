package main.gui

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart

class LossChart(xLabel: String, yLabel: String) : LineChart<Number, Number>(NumberAxis(), NumberAxis())
{
    init
    {
        this.xAxis.label = xLabel
        this.yAxis.label = yLabel
    }

    fun addPoint(label: String, point: Double)
    {
        var series = this.getSeries(label)
        if (series == null)
        {
            series = XYChart.Series()
            series.name = label
            this.data.add(series)
        }

        series.data.add(XYChart.Data(series.data.size + 1, point))
    }

    fun reset()
    {
        this.data.clear()
    }

    private fun getSeries(label: String): Series<Number, Number>?
    {
        return this.data.firstOrNull { it.name == label }
    }
}
