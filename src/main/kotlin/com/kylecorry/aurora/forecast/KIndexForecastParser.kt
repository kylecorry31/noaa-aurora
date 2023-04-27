package com.kylecorry.aurora.forecast

import java.time.ZonedDateTime

internal object KIndexForecastParser {

    fun parse(data: List<Array<String?>>): List<KIndexForecast> {
        return data.drop(1).map {
            val time = ZonedDateTime.parse(it[0]!!.replace(" ", "T") + "Z")
            val kIndex = it[1]!!.toFloat()
            val isObserved = it[2] == "observed"
            val level = it[3]
            KIndexForecast(time, kIndex, isObserved, level)
        }
    }

}