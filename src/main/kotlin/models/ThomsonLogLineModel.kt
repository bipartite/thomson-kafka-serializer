package models

import com.google.gson.annotations.SerializedName
import com.maxmind.geoip2.record.*
import geoip.GeoLiteReader
import geoip.LocationFromIp
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.time.measureTimedValue


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
    val payload : ThomsonLogLineDataClass
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

            try {
                var strippedPayload = payload.replace("\"", "")
//            println("original payload $strippedPayload")

                val dateFormat = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy")

                // Need to handle 3 formats
//            "Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]"
//            "Jan 12 17:34:12 2021 SYSLOG[0]: [Host 192.168.0.1] ICMP (type 3) 24.193.175.197 --> 82.181.71.193 DENY: Firewall interface access request"
//            "Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request"
//            "Jan 31 12:00:21 2021 SYSLOG[0]: [Host 192.168.0.1]  Time Of Day established "

                //remove message repeated if exists
                strippedPayload = strippedPayload.replace("(message repeated [0-9] times:)".toRegex(), "")
                // and HOST 192.168.0.1
                strippedPayload = strippedPayload.replace("(\\[Host 192.168.0.1\\])".toRegex(), "")
                // split at SYSLOG & create date
                var strs = strippedPayload.split("\\s(SYSLOG\\[0\\]\\:)".toRegex())
                    .map { it.replace("  ", " ") }

//            println("date removed and stripped ")
//            strs.mapIndexed { idx, v -> println("$idx : $v") }
                val strToDate = LocalDate.parse(strs[0], dateFormat)
                this.thomsonLogLineModel.date = strToDate

                var (srcStr, dstStr) = strs[1]
                    .replace("(\\[|\\])".toRegex(), "").split("(-->)".toRegex())
                    .map { it.trim() }

//            println("sorsa : $srcStr")
//            println("desti : $dstStr")

                //ip regex pattern
                val IP_REGEXP = "(?:[0-9]{1,3}\\.){3}[0-9]{1,3}"
                val IP_PATTERN = Pattern.compile(IP_REGEXP)

                val srcIpMatcher = IP_PATTERN.matcher(srcStr)
                if(srcIpMatcher.find()) {
                    this.thomsonLogLineModel.sourceIp = srcIpMatcher.group(0)
    //                println("sorsa ip : ${this.thomsonLogLineModel.sourceIp}")
                }
                val dstIpmatcher = IP_PATTERN.matcher(dstStr)
                if(dstIpmatcher.find()) {
                    this.thomsonLogLineModel.destintationIp = dstIpmatcher.group(0)
    //                println("dst ip : ${this.thomsonLogLineModel.destintationIp}")
                }

                val portRegexp = "(?<=,)(\\d+)(?:\\s+|\$)"
                val portPattern = Pattern.compile(portRegexp)
                val srcPortMatcher = portPattern.matcher(srcStr)
                val dstPortMatcher = portPattern.matcher(dstStr)

                if(srcPortMatcher.find()) {
                    this.thomsonLogLineModel.sourcePort = srcPortMatcher.group(0).toLong()
    //                println("src port ${this.thomsonLogLineModel.sourcePort}")
                }

                if(dstPortMatcher.find()) {
                    this.thomsonLogLineModel.destinationPort = dstPortMatcher.group(0).trim().toLong()
    //                println("dst port ${this.thomsonLogLineModel.destinationPort}")
                }

                val protocolRegexp = "(UDP)|(TCP)|(ICMP \\(type [0-9]\\))"
                val protocolPattern = Pattern.compile(protocolRegexp)
                val protocolMatcher = protocolPattern.matcher(srcStr)
                if(protocolMatcher.find()) {
                    this.thomsonLogLineModel.protocol = protocolMatcher.group(0)
    //                println("protocol ${this.thomsonLogLineModel.protocol}")
                }
                val ruleRegexp = "(DENY)|(ALLOW)"
                val rulePattern = Pattern.compile(ruleRegexp)
                val ruleMatcher = rulePattern.matcher(dstStr)
                if(ruleMatcher.find()) {
                    this.thomsonLogLineModel.rule = ruleMatcher.group(0)
    //                println("rule ${this.thomsonLogLineModel.rule}")
                }

                val ruleInfoRegexp = "(?<=:\\s)(.*)(?:\\s+|\$)"
                val ruleInfoPattern = Pattern.compile(ruleInfoRegexp)
                val ruleInfoMatcher = ruleInfoPattern.matcher(dstStr)
                if(ruleInfoMatcher.find()) {
                    this.thomsonLogLineModel.description = ruleInfoMatcher.group(0)
    //                println("rule Info ${this.thomsonLogLineModel.description}")
                }
                this.thomsonLogLineModel.srclocationFromIp =
                    GeoLiteReader.getGeoIpLocation(this.thomsonLogLineModel.sourceIp)
                this.thomsonLogLineModel.destlocationFromIp =
                    GeoLiteReader.getGeoIpLocation(this.thomsonLogLineModel.destintationIp)

                return this.thomsonLogLineModel
            } catch (e: Exception) {
                println("FAILED WITH STRING $payload")
            } finally {
                return this.thomsonLogLineModel
            }
        }
    }
}
