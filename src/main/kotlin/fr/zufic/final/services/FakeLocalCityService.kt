package fr.zufic.final.services

import fr.zufic.final.misc.CityData
import fr.zufic.final.misc.GeoPoint
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class FakeLocalCityService {

    var idCounter: Int = 0

    private val cityNames = List(200) { i ->
        "City_${i}_"
    }

    suspend fun getCitiesAround(
        center: GeoPoint,
        size: Int
    ): List<CityData> {

        delay(1000)

        return List(size) {
            randomCityAround(center)
        }
    }

    fun randomCityAround(center: GeoPoint): CityData {


        val geoPoint = randomGeoPointAround(center)

        return CityData(
            id = idCounter,
            name = cityNames[idCounter],
            zipcode = randomZipCode(),
            geoPoint = geoPoint
        ).also {
            idCounter++
        }
    }

    fun randomGeoPointAround(center: GeoPoint): GeoPoint {

        val maxOffset = 100.0

        val latOffset = Random.nextDouble(-maxOffset, maxOffset)
        val lonOffset = Random.nextDouble(-maxOffset, maxOffset)

        return GeoPoint(
            lat = center.lat + latOffset,
            lon = center.lon + lonOffset
        )
    }
    private fun randomZipCode(): String {
        return (10000..99999).random().toString()
    }

    suspend fun getCityById(id: Int): CityData? {
        return CityData(
            id = 1,
            name = "name",
            zipcode = "60100",
            geoPoint = GeoPoint(48.856614, 2.352222)
        )
    }
}