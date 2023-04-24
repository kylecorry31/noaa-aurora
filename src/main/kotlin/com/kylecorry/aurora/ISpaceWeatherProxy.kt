package com.kylecorry.aurora

interface ISpaceWeatherProxy {
    suspend fun get3DayForecast(): List<DailySpaceWeatherForecast>

    suspend fun getAlerts(): List<SpaceWeatherAlert>
}