
import fr.zufic.final.misc.*
import fr.zufic.final.services.*
import io.mockk.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant

class CityServiceTest {

    private lateinit var localCityService: FakeLocalCityService
    private lateinit var externalWeatherService: FakeExternalWeatherService
    private lateinit var userService: FakeUserService

    private lateinit var cityService: CityService

    @BeforeEach
    fun setup() {
        localCityService = mockk()
        externalWeatherService = mockk()
        userService = mockk()

        cityService = CityService(
            localCityService,
            externalWeatherService,
            userService
        )
    }

    @Test
    fun getCitiesInfo() = runTest {
        val city1 = mockk<CityData>()
        val city2 = mockk<CityData>()
        val city3 = mockk<CityData>()

        every { city1.name } returns "Paris"
        every { city2.name } returns "Lyon"

        coEvery {
            localCityService.getCitiesAround(any(), any())
        } returns listOf(city1, city3, city2)

        val weatherCity1 = ExternalWeatherData(
            temp = 20.0,
            humidity = 50.0,
            wind = 10.0,
            lastUpdate = Instant.now()
        )
        val weatherCity2 = ExternalWeatherData(
            temp = 0.0,
            humidity = 0.0,
            wind = 0.0,
            lastUpdate = Instant.now()
        )
        val weatherCity3 = ExternalWeatherData(
            temp = 100.0,
            humidity = 100.0,
            wind = 100.0,
            lastUpdate = Instant.now().minus(Duration.ofHours(13))
        )

        coEvery {
            externalWeatherService.getExternalWeather(city1)
        } returns weatherCity1

        coEvery {
            externalWeatherService.getExternalWeather(city2)
        } returns weatherCity2

        coEvery {
            externalWeatherService.getExternalWeather(city3)
        } returns weatherCity3

        val results = cityService
            .getCitiesInfo(GeoPoint(0.0, 0.0), 10)
            .toList()

        assertEquals(2, results.size)

        assertEquals("Paris", results[0].cityResource.info.name)
        assertEquals("Lyon", results[1].cityResource.info.name)

        assertEquals(1, results[0].count)
        assertEquals(20.0, results[0].averageWeather.temp)
        assertEquals(50.0, results[0].averageWeather.humidity)
        assertEquals(10.0, results[0].averageWeather.wind)

        assertEquals(2, results[1].count)
        assertEquals(10.0, results[1].averageWeather.temp)
        assertEquals(25.0, results[1].averageWeather.humidity)
        assertEquals(5.0, results[1].averageWeather.wind)
        assertThrows<IndexOutOfBoundsException>{ results[2] }
    }

    @Test
    fun sendWeatherToAllUsers() = runTest {
        val user1 = mockk<User>()
        val user2 = mockk<User>()
        val user3 = mockk<User>()
        val city = mockk<CityData>()

        every { user1.name } returns "Az"
        every { user2.name } returns "Test"
        every { user3.name } returns "Kotlin"
        every { user1.subscribedCities } returns listOf(1)
        every { user2.subscribedCities } returns listOf(1,2)
        every { user3.subscribedCities } returns listOf(1,2,3)

        coEvery { userService.getAllUsers() } returns listOf(user1,user2,user3)
        coEvery { localCityService.getCityById(any()) } returns city

        val weather = mockk<ExternalWeatherData>()
        every { weather.toWeatherData() } returns WeatherData(25.0, 60.0, 5.0)

        coEvery {
            externalWeatherService.getExternalWeather(city)
        } returns weather

        coEvery {
            userService.sendMailToUser(any(), any())
        } just Runs

        cityService.sendWeatherToAllUsers()

        coVerify(exactly = 1) {
            userService.sendMailToUser(
                user1,
                match { it.size == 1 }
            )
        }
        coVerify(exactly = 1) {
            userService.sendMailToUser(
                user2,
                match { it.size == 2 }
            )
        }
        coVerify(exactly = 1) {
            userService.sendMailToUser(
                user3,
                match { it.size == 3 }
            )
        }
    }
}