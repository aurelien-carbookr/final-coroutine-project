package fr.zufic.final.misc

import java.time.Instant

class ClassHolder {
}

data class ResponseInfo(
    val cityResource: CityResource,
    val averageWeather: WeatherData,
    val count: Int,
)

data class CityResource(
    val info: CityData,
    val weather: WeatherData
)

data class CityData(
    val id: Int,
    val name: String,
    val zipcode: String,
    val geoPoint: GeoPoint
)

data class WeatherData(
    val temp: Double,
    val humidity: Double,
    val wind: Double,
)

data class ExternalWeatherData(
    val temp: Double,
    val humidity: Double,
    val wind: Double,
    val lastUpdate: Instant
){
    fun toWeatherData(): WeatherData {
        return WeatherData(
            temp = temp,
            humidity = humidity,
            wind = wind
        )
    }
}

data class GeoPoint(
    val lat: Double,
    val lon: Double
)

data class User(
    val id: Int,
    val name: String,
    val subscribedCities: List<Int>,
    val email: String,
)