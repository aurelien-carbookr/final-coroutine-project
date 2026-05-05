package fr.zufic.final.services

import fr.zufic.final.misc.CityResource
import fr.zufic.final.misc.User
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class FakeUserService {

    fun getAllUsers(): List<User>{
        return List(20) { index ->
            User(
                id = index + 1,
                name = "User_${index + 1}",
                subscribedCities = List(Random.nextInt(1, 5)) {
                    Random.nextInt(1, 50)
                },
                email = "test@com",
                )
        }.distinct()
    }

    fun sendMailToUser(user: User, list: List<CityResource>) = println(
        "Mail sent to ${user.name} with ${list.size} cities"
    )
}