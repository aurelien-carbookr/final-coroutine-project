package fr.zufic.final.controllers

import ch.qos.logback.core.model.Model
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CityWebController {

    @GetMapping("/cities")
    fun getCitiesPage(model: Model): String {
        return "cities"
    }


}