package serde


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import models.RawEvent
import models.ThomsonLogLineDataClass
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import java.nio.charset.Charset

class ThomsonLogLineSerde : Serde<ThomsonLogLineDataClass> {
    private val serializer = RawEventSerializer()
    private val deserializer = RawEventDeserializer()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
    override fun close() {
        serializer.close();
        deserializer.close();
    }

    override fun serializer(): Serializer<ThomsonLogLineDataClass> = ThomsonLogLineSerializer()

    override fun deserializer(): Deserializer<ThomsonLogLineDataClass> = ThomsonLogLineDeserializer()
}

class ThomsonLogLineDeserializer : Deserializer<ThomsonLogLineDataClass> {
    private var gson = Gson()
    private val CHARSET = Charset.forName("UTF-8")

    init {
        val builder = GsonBuilder()
        gson = builder.create()
    }
    override fun deserialize(topic: String?, data: ByteArray?): ThomsonLogLineDataClass? {
        if (data == null ) return null
        val datastr = String(data, CHARSET)
        val type = object : TypeToken<String>() {}.type

        return gson?.fromJson(datastr, type)

    }


}

class ThomsonLogLineSerializer : Serializer<ThomsonLogLineDataClass> {
    private var gson = Gson()
    private val CHARSET = Charset.forName("UTF-8")

    init {
        val builder = GsonBuilder()
        gson = builder.create()
    }

    override fun serialize(topic: String?, data: ThomsonLogLineDataClass?): ByteArray? {
        if (topic == null ) return null
        return gson.toJson(topic).toByteArray(CHARSET)
    }


}