package com.kylecorry.aurora.notifications

import java.time.ZonedDateTime

data class SpaceWeatherWarning(
    override val serialNumber: Long,
    override val productId: String,
    override val messageCode: String,
    override val issueTime: ZonedDateTime,
    override val title: String,
    override val potentialImpacts: String?,
    override val message: String,
    val validFrom: ZonedDateTime,
    val validTo: ZonedDateTime,
    val warningCondition: String? = null,
    val extensionOf: Long? = null,
    val cancellationOf: Long? = null,
    val originalSerialNumber: Long = serialNumber
) : SpaceWeatherNotification