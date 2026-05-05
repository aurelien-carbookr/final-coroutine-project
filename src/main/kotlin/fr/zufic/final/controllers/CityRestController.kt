package fr.zufic.final.controllers

import fr.zufic.final.misc.GeoPoint
import fr.zufic.final.services.CityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class CityRestController(
    private val cityService: CityService
) {

    @GetMapping(
        "/api/cities/stream",
    )
    fun streamCities(): SseEmitter {
        val emitter = SseEmitter(0L)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cityService.getCitiesInfo(
                    center = GeoPoint(48.856614, 2.352222),
                    size = 100
                ).collect { responseInfo ->
                    emitter.send(responseInfo)
                }
                emitter.complete()
            } catch (e: Exception) {
                emitter.completeWithError(e)
            }
        }
        return emitter
    }

    @GetMapping(
        "/api/cities/daily",
    )
    fun dailyWeatherCities(): Unit {
        runBlocking { cityService.sendWeatherToAllUsers() }
    }
}