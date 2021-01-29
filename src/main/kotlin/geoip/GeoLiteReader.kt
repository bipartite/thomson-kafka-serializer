package geoip

import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse
import com.maxmind.geoip2.record.*
import java.io.File
import java.net.InetAddress

data class LocationFromIp(
    val country: Country,
    val subdivision: Subdivision,
    val city: City,
    val postal: Postal,
    val location: Location
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
            val response: CityResponse = reader.city(ipAddress)
            val country: Country = response.getCountry()
            val subdivision: Subdivision = response.getMostSpecificSubdivision()
            val city: City = response.getCity()
            val postal: Postal = response.getPostal()
            val location: Location = response.getLocation()

            return LocationFromIp(
                country = country,
                subdivision = subdivision,
                city = city,
                postal = postal,
                location = location
            )

        }

    }
}