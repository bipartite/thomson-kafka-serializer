import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import models.ThomsonLogLineModel
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import serde.RawEventAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals


class AppTester {

    companion object {
        private val LOG = LoggerFactory.getLogger(AppTester::class.java)
    }

    @Test
    fun testRegex() {
        val adapter = RawEventAdapter()
        val gson = GsonBuilder()

        val builder = gson.create()

        val json : String = "{ \"payload\":\"Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request \"}"
        val map: Map<String, String> = Gson().fromJson(json, object : TypeToken<Map<String?, String?>?>() {}.type)
        val tempStr = map.get("payload")
        val dateFormat = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy")
        var strs : List<String> = tempStr.toString()
            .split("\\s(SYSLOG\\[0\\]\\:)".toRegex())
            .map { it.replace("  ", " ") }
        println(strs[0])

        val strToDate = LocalDate.parse(strs[0], dateFormat)
//        strs = strs.map { it.replace("\\s+".toRegex(), "") }
        println(strToDate.toString())

        assertEquals(strToDate, 1)
    }

    @Test
    fun ModelFromString() {
        val string = "Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request"

        val t = ThomsonLogLineModel(string)

        println("protocol tulee ${t.protocol}")

        println("teeksi tulee ${t.toString()}")
    }
}