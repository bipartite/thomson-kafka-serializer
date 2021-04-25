package models

import com.google.gson.annotations.SerializedName
import geoip.GeoLiteReader
import geoip.LocationFromIp
import org.apache.logging.log4j.LogManager
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern


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
        private val logger = LogManager.getLogger(ThomsonLogLineDataClass::class.java)

        fun parseData(logLine: String) : ThomsonLogLineDataClass {

            var thomsonLogLineModel = ThomsonLogLineDataClass()
            try {
                // Need to handle 4 cases
//            "Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]"
//            "Jan 12 17:34:12 2021 SYSLOG[0]: [Host 192.168.0.1] ICMP (type 3) 24.193.175.197 --> 82.181.71.193 DENY: Firewall interface access request"
//            "Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request"
//            "Jan 31 12:00:21 2021 SYSLOG[0]: [Host 192.168.0.1]  Time Of Day established "

                val separatorRegexp = "(-->)"
                val separatorPattern = Pattern.compile(separatorRegexp)
                val separatorMatcher = separatorPattern.matcher(logLine)

                // When "-->" not found return
                if(!separatorMatcher.find()) {
                    logger.debug("--> not found, discarding string $logLine")
                    return thomsonLogLineModel
                }

                //Thomson produces " at the beginning and end the log string
                var strippedPayload = if (logLine[0] == '"') logLine.substring(1, logLine.length-1) else logLine

//                var strippedPayload = logLine.replace("\"", "").trim()
//                logger.debug("original payload $strippedPayload")

                //strip date part from the rest of the string
                var dateStr = strippedPayload.substring(0, 20)
                var payload = strippedPayload.substring(51)
                logger.debug("payload [0] ${payload[0]}")

                // Parse date
                // Two different date formats
                val dateFormats = Arrays.asList("MMM d HH:mm:ss yyyy", "MMM  d HH:mm:ss yyyy")

                dateFormats.map {
                    try {
                        val dateFromStr = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(it))
                        thomsonLogLineModel.date = dateFromStr
                    } catch (e: Exception) {}
                }

                //Split log string to source and destination part
                var (srcStr, dstStr) = payload
                    .split("(-->)".toRegex())
                    .map { it.trim() }

                logger.debug("sorsa : $srcStr")
                logger.debug("desti : $dstStr")

                //ip regex pattern
                val IP_REGEXP = "(?:[0-9]{1,3}\\.){3}[0-9]{1,3}"
                val IP_PATTERN = Pattern.compile(IP_REGEXP)

                val srcIpMatcher = IP_PATTERN.matcher(srcStr)
                if(srcIpMatcher.find()) {
                    thomsonLogLineModel.sourceIp = srcIpMatcher.group(0)
                    logger.debug("sorsa ip : ${thomsonLogLineModel.sourceIp}")
                }
                val dstIpmatcher = IP_PATTERN.matcher(dstStr)
                if(dstIpmatcher.find()) {
                    thomsonLogLineModel.destintationIp = dstIpmatcher.group(0)
                    logger.debug("dst ip : ${thomsonLogLineModel.destintationIp}")
                }

                val portRegexp = "(?<=,)(\\d+)(?:\\s+|\$)"
                val portPattern = Pattern.compile(portRegexp)
                val srcPortMatcher = portPattern.matcher(srcStr)
                val dstPortMatcher = portPattern.matcher(dstStr)

                if(srcPortMatcher.find()) {
                    thomsonLogLineModel.sourcePort = srcPortMatcher.group(0).toLong()
                    logger.debug("src port ${thomsonLogLineModel.sourcePort}")
                }

                if(dstPortMatcher.find()) {
                    thomsonLogLineModel.destinationPort = dstPortMatcher.group(0).trim().toLong()
                    logger.debug("dst port ${thomsonLogLineModel.destinationPort}")
                }

                val protocolRegexp = "(UDP)|(TCP)|(ICMP \\(type [0-9]\\))"
                val protocolPattern = Pattern.compile(protocolRegexp)
                val protocolMatcher = protocolPattern.matcher(srcStr)
                if(protocolMatcher.find()) {
                    thomsonLogLineModel.protocol = protocolMatcher.group(0)
                    logger.debug("protocol ${thomsonLogLineModel.protocol}")
                }
                val ruleRegexp = "(DENY)|(ALLOW)"
                val rulePattern = Pattern.compile(ruleRegexp)
                val ruleMatcher = rulePattern.matcher(dstStr)
                if(ruleMatcher.find()) {
                    thomsonLogLineModel.rule = ruleMatcher.group(0)
                    logger.debug("rule ${thomsonLogLineModel.rule}")
                }

                val ruleInfoRegexp = "(?<=:\\s)(.*)(?:\\s+|\$)"
                val ruleInfoPattern = Pattern.compile(ruleInfoRegexp)
                val ruleInfoMatcher = ruleInfoPattern.matcher(dstStr)
                if(ruleInfoMatcher.find()) {
                    thomsonLogLineModel.description = ruleInfoMatcher.group(0)
                    logger.debug("rule Info ${thomsonLogLineModel.description}")
                }
                thomsonLogLineModel.srclocationFromIp =
                    GeoLiteReader.getGeoIpLocation(thomsonLogLineModel.sourceIp)
                thomsonLogLineModel.destlocationFromIp =
                    GeoLiteReader.getGeoIpLocation(thomsonLogLineModel.destintationIp)

                return thomsonLogLineModel
            } catch (e: Exception) {
                logger.debug("FAILED WITH STRING $logLine")
                logger.debug(e.toString())
            } finally {
                return thomsonLogLineModel
            }
        }
    }
}
