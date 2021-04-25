package serde

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import models.RawEvent
import java.io.IOException


//{
//    "schema":
//    {
//        "type":"string",
//        "optional":false
//    },
//    "payload":"Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request "
//}

class RawEventAdapter: TypeAdapter<RawEvent>() {

    private val gson = Gson()

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: RawEvent?) {
        if ( value == null ) {
            out.nullValue()
            return
        }

//        val tempStr = value.payload

//        val dateFormat = DateTimeFormatter.ofPattern("MMM d hh:mm:ss yyyy")
//        var strs = tempStr.split("\\s(SYSLOG\\[0\\]\\:)".toRegex())
//            .map { it.replace("  ", " ") }

//        val strToDate = LocalDate.parse(strs[0], dateFormat)






    }

    override fun read(`in`: JsonReader?): RawEvent {
    TODO()
//        val eventJson = gson.fromJson<Any>(`in`, RawEvent::class.java) as RawEvent
    }
}