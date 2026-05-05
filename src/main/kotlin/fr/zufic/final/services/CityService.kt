package fr.zufic.final.services

import fr.zufic.final.misc.CityResource
import fr.zufic.final.misc.GeoPoint
import fr.zufic.final.misc.ResponseInfo
import fr.zufic.final.misc.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@Service
class CityService(
    private val localCityService: FakeLocalCityService,
    private val externalWeatherService: FakeExternalWeatherService,
    private val userService: FakeUserService,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getCitiesInfo(
        center: GeoPoint,
        size: Int
    ): Flow<ResponseInfo> {

        val localCities = localCityService.getCitiesAround(center, size)
        val total: Float = localCities.size.toFloat()

        val count = AtomicInteger(0)
        val sumTemp = AtomicReference(0.0)
        val sumHumidity = AtomicReference(0.0)
        val sumWind = AtomicReference(0.0)

        return localCities
            .asFlow()
            .flatMapMerge(concurrency = 5) { city ->
                flow {
                    val weather = runCatching { externalWeatherService.getExternalWeather(city)}
                    weather.onSuccess { emit(city to it) }
                    weather.onFailure {
                        println("fetch weather for ${city.name} failed: ${it.message}")
                    }
                }
            }
            .filter { (_, weather) ->
                weather.lastUpdate.isAfter(
                    Instant.now().minus(
                        Duration.ofHours(5)
                    )
                )
            }
            .map { (city, weather) ->

                val currentCount = count.incrementAndGet()
                sumTemp.updateAndGet { it + weather.temp }
                sumHumidity.updateAndGet { it + weather.humidity }
                sumWind.updateAndGet { it + weather.wind }

                val averageWeather = WeatherData(
                    temp = sumTemp.get() / currentCount,
                    humidity = sumHumidity.get() / currentCount,
                    wind = sumWind.get() / currentCount
                )

                ResponseInfo(
                    cityResource = CityResource(
                        info = city,
                        weather = weather.toWeatherData()
                    ),
                    averageWeather = averageWeather,
                    count = currentCount,
                )
            }
            .onStart {
                println("🟢 Start streaming $total cities")
            }
            .onEach {
                println("📡 ${it.count}% - ${it.cityResource.info.name}")
            }
            .onCompletion {
                println("✅ Stream completed")
            }
    }

    suspend fun sendWeatherToAllUsers() = coroutineScope {
        val users = userService.getAllUsers()

        users.map { user ->
            async(Dispatchers.IO) {
                println("📧 Preparing weather for ${user.name}")
                val citiesWithWeather = user.subscribedCities.map { cityId ->
                    async(Dispatchers.IO) {
                        val city = localCityService.getCityById(cityId)
                        city?.let {
                            val weather = externalWeatherService.getExternalWeather(it)
                            CityResource(it, weather.toWeatherData())
                        }
                    }
                }.awaitAll().filterNotNull()
                userService.sendMailToUser(user, citiesWithWeather)
            }
        }.awaitAll()
    }
}

