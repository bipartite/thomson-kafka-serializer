package geoip

import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse
import com.maxmind.geoip2.record.*
import java.io.File
import java.net.InetAddress

data class LocationFromIp(
    val country: Country? = null,
    val subdivision: Subdivision? = null,
    val city: City? = null,
    val postal: Postal? = null,
    val location: Location? = null,
    val errorstr: String = ""
)

class GeoLiteReader {

    // This creates the DatabaseReader object. To improve performance, reuse
    // the object across lookups. The object is thread-safe.

    // This creates the DatabaseReader object. To improve performance, reuse
    // the object across lookups. The object is thread-safe.
    companion object {
        private val database = File("/home/juhak/geoip/GeoLite2-City.mmdb")
        private val reader: DatabaseReader = DatabaseReader.Builder(database).withCache(CHMCache()).build()
        fun getGeoIpLocation(ipString: String): LocationFromIp {

            val ipAddress = InetAddress.getByName(ipString)

            // Replace "city" with the appropriate method for your database, e.g.,
            // "country".

            // Replace "city" with the appropriate method for your database, e.g.,
            // "country".

            val locationFromIp : LocationFromIp = try {
                val response: CityResponse = reader.city(ipAddress)
                val country: Country = response.getCountry()
                val subdivision: Subdivision = response.getMostSpecificSubdivision()
                val city: City = response.getCity()
                val postal: Postal = response.getPostal()
                val location: Location = response.getLocation()
                LocationFromIp(
                    country = country,
                    subdivision = subdivision,
                    city = city,
                    postal = postal,
                    location = location
                )
            } catch (exception: Exception) {
                LocationFromIp(
                    errorstr = "AddressNotFoundException: The address $ipString is not in the database."
                )
            } finally {

            }

            return locationFromIp

        }

    }
}