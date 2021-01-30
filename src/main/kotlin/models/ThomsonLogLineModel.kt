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

data class ThomsonLogLineDataClass(
    var date : LocalDate = LocalDate.now(),
    var host : String = "192.168.0.1",
    var protocol : String = "",
    var sourcePort : Long? = null,
    var sourceIp : String = "",
    var destintationIp : String = "",
    var destinationPort : Long? = null,
    var description : String = "",
    var payload : String = "",
    var rule : String = "",
    var destlocationFromIp: LocationFromIp? = null,
    var srclocationFromIp: LocationFromIp? = null
) : Serializable

class ParseLogDataFromString() : Serializable {
    companion object {
        private var thomsonLogLineModel = ThomsonLogLineDataClass()

        fun parseData(payload: String) : ThomsonLogLineDataClass {
            var strippedPayload = payload.replace("\"", "")
            println("original payload $strippedPayload")

            val dateFormat = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy")

            // split at SYSLOG & create date
            var strs = strippedPayload.split("\\s(SYSLOG\\[0\\]\\:)|(message repeated [0-9] times:)|(-->)".toRegex())
                .map { it.replace("  ", " ") }

            println("splitattu stringi:")
            strs.mapIndexed { idx, v -> println("$idx : $v") }

            val strToDate = LocalDate.parse(strs[0], dateFormat)
            this.thomsonLogLineModel.date = strToDate


            if ( strippedPayload.contains("repeated")) {
//    Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]
            } else {
//      0 1     2            3   4                   5   6                  7     8        9         10     11
//        [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request
                println(strs[1])
                var datastr : List<String> = strs[1].split("\\s+".toRegex())
                println("datastr $datastr")
                this.thomsonLogLineModel.protocol = datastr[3].replace("\\s+".toRegex(), "")
                val s = datastr[4].split(",")
                this.thomsonLogLineModel.sourceIp = s[0]
                this.thomsonLogLineModel.sourcePort = s[1].toLong()
                val d = datastr[6].split(",")
                this.thomsonLogLineModel.destintationIp = d[0]
                this.thomsonLogLineModel.destinationPort = d[1].toLong()
                this.thomsonLogLineModel.rule = datastr[7].replace(":", "")
                this.thomsonLogLineModel.description = datastr.subList(8, datastr.size).joinToString()
                this.thomsonLogLineModel.srclocationFromIp = GeoLiteReader.getGeoIpLocation(this.thomsonLogLineModel.sourceIp)
                this.thomsonLogLineModel.destlocationFromIp = GeoLiteReader.getGeoIpLocation(this.thomsonLogLineModel.destintationIp)


            }

            return this.thomsonLogLineModel
        }
    }
}

class ThomsonLogLineModel(payload: String) : Serializable {
    var date : LocalDate = LocalDate.now()
    var host : String = "192.168.0.1"
    var protocol : String = ""
    var sourceIp : String = ""
    var sourcePort : Long? = null
    var destintationIp : String = ""
    var destinationPort : Long? = null
    var rule : String = ""
    var description : String = ""
    var payload : String = ""
    var destlocationFromIp: LocationFromIp? = null
    var srclocationFromIp: LocationFromIp? = null

    var thomsonData = ThomsonLogLineDataClass()

    init {
        this.payload = payload.replace("\"", "")
        convert()
    }

    //  Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request
    private fun convert() {

        val dateFormat = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy")

        println("original string ${this.payload}")
        // split at SYSLOG & create date
        var strs = this.payload.split("\\s(SYSLOG\\[0\\]\\:)|(message repeated [0-9] times:)".toRegex())
            .map { it.replace("  ", " ") }
        println("splitattu stringi $strs")

        val strToDate = LocalDate.parse(strs[0], dateFormat)
        this.date = strToDate
        println("Stripped payload ${this.payload}")


        if ( this.payload.contains("repeated")) {
//    Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]
//            return thomsonData
        } else {
//      0 1     2            3   4                   5   6                  7     8        9         10     11
//        [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request
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

    }

    override fun toString(): String {
        return super.toString()
    }
}