package com.kylecorry.aurora.notifications

import java.time.ZonedDateTime

data class SpaceWeatherAlert(
    override val serialNumber: Long,
    override val productId: String,
    override val messageCode: String,
    override val issueTime: ZonedDateTime,
    override val title: String,
    override val potentialImpacts: String?,
    override val message: String,
    val continuationOf: Long? = null,
) : SpaceWeatherNotification