package models

import com.google.gson.annotations.SerializedName
import com.maxmind.geoip2.record.*
import geoip.GeoLiteReader
import geoip.LocationFromIp
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter


//{"schema":
//    {"type":"string",
//    "optional":false
//    },
//    "payload":"Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request "
//}

// thomson_ultamate_logger

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
    lateinit var sourceIp : String
    var sourcePort : Long? = null
    lateinit var destintationIp : String
    var destinationPort : Long? = null
    lateinit var rule : String
    lateinit var description : String
    var payload : String
    var destlocationFromIp: LocationFromIp? = null
    var srclocationFromIp: LocationFromIp? = null

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

//      0 1     2            3   4                   5   6                  7     8        9         10     11
//        [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request
        println(strs[1])
        var datastr : List<String> = strs[1].split("\\s+".toRegex())
        println("datastr $datastr")
        this.protocol = datastr[3].replace("\\s+".toRegex(), "")
        val s = datastr[4].split(",")
        this.sourceIp = s[0]
        this.sourcePort = s[1].toLong()
        val d = datastr[6].split(",")
        this.destintationIp = d[0]
        this.destinationPort = d[1].toLong()
        this.rule = datastr[7].replace(":", "")
        this.description = datastr.subList(8, datastr.size).joinToString()
        this.srclocationFromIp = GeoLiteReader.getGeoIpLocation(this.sourceIp)
        this.destlocationFromIp = GeoLiteReader.getGeoIpLocation(this.destintationIp)
    }

    override fun toString(): String {
        return super.toString()
    }
}