package com.kylecorry.aurora

import java.time.ZonedDateTime

data class KpIndexForecast(val start: ZonedDateTime, val end: ZonedDateTime, val kp: Float)