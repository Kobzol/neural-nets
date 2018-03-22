package main.racer

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

data class Sample(val input: DoubleArray, val output: DoubleArray)

class SamplePersister
{
    fun persistSamples(samples: List<Sample>, path: Path)
    {
        val json = Gson().toJson(samples)
        Files.write(path, json.toByteArray(Charset.forName("UTF-8")))
    }

    fun loadSamples(path: Path): List<Sample>
    {
        return Gson().fromJson<List<Sample>>(String(Files.readAllBytes(path), Charset.forName("UTF-8")),
                object : TypeToken<List<Sample>>(){}.type)
    }
}
