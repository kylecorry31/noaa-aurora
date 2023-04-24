package com.kylecorry.aurora

import java.time.ZonedDateTime

data class SpaceWeatherAlert(
    val alert: String,
    val issuedOn: ZonedDateTime,
    val message: String
)