import com.kylecorry.aurora.NOAASpaceWeatherProxy
import com.kylecorry.aurora.notifications.SpaceWeatherAlert
import com.kylecorry.aurora.notifications.SpaceWeatherSummary
import kotlinx.coroutines.runBlocking

fun main() {
    val proxy = NOAASpaceWeatherProxy()
    runBlocking {
        println(proxy.getKIndexForecast())
        val all = proxy.getNotifications(true)
        println(all.map { it.messageCode })
    }
}