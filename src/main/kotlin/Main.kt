import com.kylecorry.aurora.NOAASpaceWeatherProxy
import kotlinx.coroutines.runBlocking

fun main() {
    val proxy = NOAASpaceWeatherProxy()
    runBlocking {
//        println(proxy.get3DayForecast())
        println(proxy.getAlerts().map { it.issuedOn to it.alert }.joinToString("\n"))
    }
}