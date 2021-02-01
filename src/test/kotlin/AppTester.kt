import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse
import com.maxmind.geoip2.record.*
import models.ParseLogDataFromString
import models.ThomsonLogLineModel
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import serde.RawEventAdapter
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import java.net.InetAddress





class AppTester {

    companion object {
        private val LOG = LoggerFactory.getLogger(AppTester::class.java)
    }


//    Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]
//    Jan 12 17:34:12 2021 SYSLOG[0]: [Host 192.168.0.1] UDP 192.168.0.14,17500 --> 192.168.0.255,17500 ALLOW: Inbound access request
//    Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,17500 --> 192.168.0.255,17500 ALLOW: Inbound access request ]
//    Jan 12 17:34:17 2021 SYSLOG[0]: [Host 192.168.0.1] UDP 192.168.0.14,138 --> 192.168.0.255,138 ALLOW: Inbound access request
//    Jan 12 17:34:17 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,138 --> 192.168.0.255,138 ALLOW: Inbound access request ]
//    Jan 12 17:34:23 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 192.168.0.14,51891 --> 52.34.254.140,443 ALLOW: Outbound access request
//    Jan 12 17:34:24 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 192.168.0.14,51892 --> 52.142.125.222,443 ALLOW: Outbound access request

    @Test
    fun testRegex1() {
        val string = "Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request"

        val t = ThomsonLogLineModel(string)

        assertEquals("TCP", t.protocol)

        assertEquals("Firewall, interface, access, request", t.description)
        println("source ip tulee ${t.sourceIp}")
        assertEquals(45362, t.sourcePort)
        println("dest ip tulee ${t.destintationIp}")
        assertEquals(3389, t.destinationPort)
        assertEquals("DENY", t.rule)
        assertEquals("TCP", t.protocol)

        println(t.srclocationFromIp?.city?.name)
    }

    @Test
    fun testRegex2() {
        val string = "Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]"
        val string2 = "Jan 12 17:34:12 2021 SYSLOG[0]: [Host 192.168.0.1] ICMP (type 3) 24.193.175.197 --> 82.181.71.193 DENY: Firewall interface access request"
        val string3 = "Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request"

//        val t = ThomsonLogLineModel(string)

        val t = ParseLogDataFromString.parseData(string)

        val t2 = ParseLogDataFromString.parseData(string2)

        val t3 = ParseLogDataFromString.parseData(string3)

        assertEquals("TCP", t.protocol)
        assertEquals("Firewall interface access request", t.description)
        println("source ip tulee ${t.sourceIp}")
        assertEquals(45362, t.sourcePort)
        println("dest ip tulee ${t.destintationIp}")
        assertEquals(3389, t.destinationPort)
        assertEquals("DENY", t.rule)
        assertEquals("TCP", t.protocol)

        println(t.srclocationFromIp?.city?.name)
    }

    @Test
    fun geoipDbTest() {
        // A File object pointing to your GeoIP2 or GeoLite2 database
        // A File object pointing to your GeoIP2 or GeoLite2 database
        val database = File("/home/juhak/geoip/GeoLite2-City.mmdb")

        // This creates the DatabaseReader object. To improve performance, reuse
        // the object across lookups. The object is thread-safe.

        // This creates the DatabaseReader object. To improve performance, reuse
        // the object across lookups. The object is thread-safe.
        val reader: DatabaseReader = DatabaseReader.Builder(database).withCache(CHMCache()).build()

        val ipAddress = InetAddress.getByName("128.101.101.101")

        // Replace "city" with the appropriate method for your database, e.g.,
        // "country".

        // Replace "city" with the appropriate method for your database, e.g.,
        // "country".
        val response: CityResponse = reader.city(ipAddress)

        val country: Country = response.getCountry()
        println(country.getIsoCode()) // 'US'

        println(country.getName()) // 'United States'

        println(country.getNames().get("zh-CN")) // '美国'

        val subdivision: Subdivision = response.getMostSpecificSubdivision()
        println(subdivision.getName()) // 'Minnesota'

        println(subdivision.getIsoCode()) // 'MN'
        val city: City = response.getCity()
        println(city.getName()) // 'Minneapolis'
        val postal: Postal = response.getPostal()
        println(postal.getCode()) // '55455'

        val location: Location = response.getLocation()
        println(location.getLatitude()) // 44.9733

        println(location.getLongitude()) // -93.2323


    }

}