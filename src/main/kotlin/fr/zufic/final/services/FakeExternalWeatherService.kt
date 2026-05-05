package fr.zufic.final.services

import fr.zufic.final.misc.CityData
import fr.zufic.final.misc.ExternalWeatherData
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import kotlin.random.Random
import java.time.Instant
import java.time.Duration

@Service
class FakeExternalWeatherService {

    suspend fun getExternalWeather(city: CityData): ExternalWeatherData {

        println("🚀 START getWeather for city=${city.name} at=${System.currentTimeMillis()}")
        delay((Random.nextDouble(1000.0, 10000.0)).toLong())
        println("✅ END getWeather for city=${city.name} at=${System.currentTimeMillis()}")

        return ExternalWeatherData(
            temp = Random.nextDouble(-10.0, 35.0),
            humidity = Random.nextDouble(-10.0, 35.0),
            wind = Random.nextDouble(0.0, 20.0),
            lastUpdate = Instant.now().minus(
                Duration.ofHours(
                    Random.nextLong(0, 13)
                )
            )
        )
    }
}