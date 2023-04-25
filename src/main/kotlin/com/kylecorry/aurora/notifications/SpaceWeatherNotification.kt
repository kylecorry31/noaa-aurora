package com.kylecorry.aurora.notifications

import java.time.ZonedDateTime

interface SpaceWeatherNotification {
    val serialNumber: Long
    val productId: String
    val messageCode: String
    val issueTime: ZonedDateTime
    val title: String
    val potentialImpacts: String?
    val message: String
}