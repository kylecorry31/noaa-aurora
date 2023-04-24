package com.kylecorry.aurora

import java.time.LocalDate

/**
 * A space weather forecast for a given day
 * @param date The date of the forecast (UTC)
 * @param kp The Kp index forecast for the day
 */
data class DailySpaceWeatherForecast(
    val date: LocalDate,
    val kp: List<KpIndexForecast>
)