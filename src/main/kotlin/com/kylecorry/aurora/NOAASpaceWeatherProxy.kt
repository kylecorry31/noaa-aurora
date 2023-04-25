package com.kylecorry.aurora

import com.google.gson.Gson
import com.kylecorry.aurora.notifications.NotificationMerger
import com.kylecorry.aurora.notifications.NotificationParser
import com.kylecorry.aurora.notifications.SpaceWeatherNotification
import com.kylecorry.aurora.notifications.SpaceWeatherWarning
import kotlinx.coroutines.coroutineScope
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.net.ssl.HttpsURLConnection

class NOAASpaceWeatherProxy : ISpaceWeatherProxy {

    override suspend fun get3DayForecast(): List<DailySpaceWeatherForecast> {
        val forecastText = get("https://services.swpc.noaa.gov/text/3-day-forecast.txt")

        val issuedDate = getIssuedDate(forecastText)
        val kp = getKpIndexForecast(forecastText, issuedDate)

        return kp.groupBy { it.start.toLocalDate() }.map { (date, kp) -> DailySpaceWeatherForecast(date, kp) }
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
                it.validFrom.isBefore(time) && it.validTo.isAfter(time)
            } else {
                it.issueTime >= time.minusDays(1)
            }
        }
    }

    private fun getKpIndexForecast(
        forecastText: String, issuedDate: ZonedDateTime
    ): List<KpIndexForecast> {
        val header = forecastText.indexOf("NOAA Kp index breakdown")

        val tableStartIdx = forecastText.indexOf("\n", header) + 1
        val tableEndIdx = forecastText.indexOf("Rationale:", tableStartIdx)

        val rows =
            forecastText.substring(tableStartIdx, tableEndIdx).split("\n").map { it.trim() }.filter { it.isNotBlank() }

        // The first row indicates the day in MMM DD format
        val days = rows[0].split("  ").filter { it.isNotBlank() }.map { it.trim() }.map { day ->
            val (month, dayOfMonth) = day.split(" ")
            val monthValue = getMonthValue(month)
            // Handle year rollover
            val year = if (monthValue < issuedDate.monthValue) {
                issuedDate.year + 1
            } else {
                issuedDate.year
            }
            LocalDate.of(year, monthValue, dayOfMonth.toInt())
        }

        // Extract the time range
        val times = rows.drop(1).map { it.split("  ")[0] }.map { time ->
            val (start, end) = time.split("-")
            // Take the hour from start and end
            val startHour = start.substring(0, 2).toInt()
            val endHour = end.substring(0, 2).toInt()
            startHour to endHour
        }

        // Extract the Kp values
        val kpValues = rows.drop(1).map { it.split(" ") }.map { it.drop(1) }
            .map { it.mapNotNull { it.split(" ")[0].toFloatOrNull() } }

        // Combine the data
        val data = mutableListOf<KpIndexForecast>()

        for (i in days.indices) {
            val day = days[i]
            for (j in times.indices) {
                val (startHour, endHour) = times[j]
                val kp = kpValues[j][i]
                val start = day.atTime(startHour, 0).atZone(ZoneId.of("UTC"))

                // Handle end of day midnight rollover
                val end = if (startHour > endHour) {
                    day.plusDays(1).atTime(endHour, 0).atZone(ZoneId.of("UTC"))
                } else {
                    day.atTime(endHour, 0).atZone(ZoneId.of("UTC"))
                }

                data.add(KpIndexForecast(start, end, kp))
            }
        }

        return data

    }

    private fun getMonthValue(month: String): Int {
        return when (month) {
            "Jan" -> 1
            "Feb" -> 2
            "Mar" -> 3
            "Apr" -> 4
            "May" -> 5
            "Jun" -> 6
            "Jul" -> 7
            "Aug" -> 8
            "Sep" -> 9
            "Oct" -> 10
            "Nov" -> 11
            "Dec" -> 12
            else -> throw IllegalArgumentException("Invalid month")
        }
    }

    private fun getIssuedDate(forecastText: String): ZonedDateTime {
        // Use regex to retrieve the issued date
        // Format: :Issued: 2023 Mar 25 1230 UTC
        val regex = Regex(":Issued: (\\d{4}) (\\w{3}) (\\d{2}) (\\d{2})(\\d{2}) UTC")
        val match = regex.find(forecastText)
        val (year, month, day, hour, minute) = match!!.destructured

        val monthValue = getMonthValue(month)

        // Convert to zoned date time
        return ZonedDateTime.of(
            year.toInt(), monthValue, day.toInt(), hour.toInt(), minute.toInt(), 0, 0, ZoneId.of("UTC")
        )
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