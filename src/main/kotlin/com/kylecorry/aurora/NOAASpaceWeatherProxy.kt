package com.kylecorry.aurora

import com.google.gson.Gson
import com.kylecorry.aurora.forecast.KIndexForecast
import com.kylecorry.aurora.forecast.KIndexForecastParser
import com.kylecorry.aurora.notifications.NotificationMerger
import com.kylecorry.aurora.notifications.NotificationParser
import com.kylecorry.aurora.notifications.SpaceWeatherNotification
import com.kylecorry.aurora.notifications.SpaceWeatherWarning
import kotlinx.coroutines.coroutineScope
import java.net.URL
import java.time.ZonedDateTime
import javax.net.ssl.HttpsURLConnection

class NOAASpaceWeatherProxy : ISpaceWeatherProxy {

    override suspend fun getKIndexForecast(): List<KIndexForecast> {
        val forecastText = get("https://services.swpc.noaa.gov/products/noaa-planetary-k-index-forecast.json")
        return KIndexForecastParser.parse(Gson().fromJson(forecastText, Array<Array<String?>>::class.java).toList())
            .sortedBy { it.time }
    }


    override suspend fun getNotifications(activeOnly: Boolean): List<SpaceWeatherNotification> {
        val alertsText = get("https://services.swpc.noaa.gov/products/alerts.json")
        val alerts = Gson().fromJson(alertsText, Array<RawSpaceWeatherAlert>::class.java)
        val all = NotificationParser.parse(alerts.toList())
        return if (activeOnly) {
            filterActiveNotifications(all)
        } else {
            all
        }
    }

    private fun filterActiveNotifications(
        notifications: List<SpaceWeatherNotification>,
        time: ZonedDateTime = ZonedDateTime.now()
    ): List<SpaceWeatherNotification> {
        return NotificationMerger.merge(notifications).filter {
            if (it is SpaceWeatherWarning) {
                it.validTo.isAfter(time)
            } else {
                it.issueTime >= time.minusDays(1)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun get(url: String): String {
        return coroutineScope {
            val connection = URL(url).openConnection() as HttpsURLConnection
            connection.inputStream.use {
                connection.inputStream.bufferedReader().readText()
            }
        }
    }

}