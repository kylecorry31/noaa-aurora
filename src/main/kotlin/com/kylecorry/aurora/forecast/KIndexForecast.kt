package com.kylecorry.aurora.forecast

import java.time.ZonedDateTime

data class KIndexForecast(val time: ZonedDateTime, val kIndex: Float, val isObserved: Boolean, val level: String?)