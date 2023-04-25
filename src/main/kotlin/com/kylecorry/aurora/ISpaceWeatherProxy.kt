package com.kylecorry.aurora

import com.kylecorry.aurora.notifications.SpaceWeatherNotification
import java.time.ZonedDateTime

interface ISpaceWeatherProxy {
    suspend fun get3DayForecast(): List<DailySpaceWeatherForecast>

    suspend fun getNotifications(activeOnly: Boolean = false): List<SpaceWeatherNotification>
}