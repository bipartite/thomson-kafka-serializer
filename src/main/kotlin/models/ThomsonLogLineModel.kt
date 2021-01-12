package models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter


//{"schema":
//    {"type":"string",
//    "optional":false
//    },
//    "payload":"Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request "
//}

data class RawEvent(
    @SerializedName("schema")
    val shema: MutableMap<String, String>,
    @SerializedName("payload")
    val payload : ThomsonLogLineModel
) : Serializable

class ThomsonLogLineModel(payload: String) : Serializable {
    lateinit var date : LocalDate
    var host : String = "192.168.0.1"
    lateinit var protocol : String
    lateinit var source : String
    lateinit var destintation :String
    lateinit var rule: String
    lateinit var description: String
    var payload: String

    init {
        this.payload = payload
        convert()
    }

    //  Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request
    private fun convert() {
        val dateFormat = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy")

        // split at SYSLOG & create date
        var strs = this.payload.split("\\s(SYSLOG\\[0\\]\\:)".toRegex())
            .map { it.replace("  ", " ") }

        val strToDate = LocalDate.parse(strs[0], dateFormat)
        this.date = strToDate
//        0     1            2   3                   4   5                  6    7         8         9      10
//        [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request
        println(strs[1])
        var datastr : List<String> = strs[1].split("\\s+".toRegex())
        println("datastr $datastr")
        this.protocol = datastr[3].replace("\\s+".toRegex(), "")
        this.source = datastr[4]
        this.destintation = datastr[5]
        this.rule = datastr[6]
        this.description = datastr.subList(7, 10).joinToString()
    }
}