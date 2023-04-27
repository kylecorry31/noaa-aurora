package com.kylecorry.aurora

import com.kylecorry.aurora.forecast.KIndexForecast
import com.kylecorry.aurora.notifications.SpaceWeatherNotification

interface ISpaceWeatherProxy {
    suspend fun getKIndexForecast(): List<KIndexForecast>

    suspend fun getNotifications(activeOnly: Boolean = false): List<SpaceWeatherNotification>
}