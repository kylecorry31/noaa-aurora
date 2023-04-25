package com.kylecorry.aurora.notifications

import com.kylecorry.aurora.RawSpaceWeatherAlert
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal object NotificationParser {

    fun parse(notifications: List<RawSpaceWeatherAlert>): List<SpaceWeatherNotification> {
        return notifications.mapNotNull { parse(it) }
    }

    private fun parse(notification: RawSpaceWeatherAlert): SpaceWeatherNotification? {

        if (notification.productId.endsWith("W")) {
            return parseWarning(notification)
        }

        if (notification.productId.endsWith("F")) {
            return parseWatch(notification)
        }

        if (notification.productId.endsWith("A")) {
            return parseAlert(notification)
        }

        if (notification.productId.endsWith("S")) {
            return parseSummary(notification)
        }

        return null
    }

    private fun parseWarning(notification: RawSpaceWeatherAlert): SpaceWeatherNotification {
        val lines = notification.message.split("\r\n").map { it.split(":").map { it.trim() } }

        val serialNumber = getLine(lines, "Serial Number")!!.toLong()
        val messageCode = getLine(lines, "Space Weather Message Code")!!
        val issueTime = parseTime(getLine(lines, "Issue Time")!!)!!
        val title = getLine(lines, "Warning", "Extended Warning", "Cancel Warning")!!
        val validFrom = getLine(lines, "Valid From")?.let { parseTime(it) } ?: issueTime
        val validTo = getLine(lines, "Valid To", "New Valid Until")?.let { parseTime(it) } ?: issueTime
        val warningCondition = getLine(lines, "Warning Condition")
        val extensionOf = getLine(lines, "Extension to Serial Number")?.toLongOrNull()
        val cancellationOf = getLine(lines, "Cancel Serial Number")?.toLongOrNull()

        val potentialImpactsIndex = notification.message.indexOf("Potential Impacts: ")
        val potentialImpacts = if (potentialImpactsIndex != -1) {
            notification.message.substring(potentialImpactsIndex + "Potential Impacts: ".length)
        } else {
            null
        }

        return SpaceWeatherWarning(
            serialNumber,
            notification.productId,
            messageCode,
            issueTime,
            title,
            potentialImpacts,
            notification.message,
            validFrom,
            validTo,
            warningCondition,
            extensionOf,
            cancellationOf
        )
    }

    private fun parseWatch(notification: RawSpaceWeatherAlert): SpaceWeatherNotification {
        val lines = notification.message.split("\r\n").map { it.split(":").map { it.trim() } }

        val serialNumber = getLine(lines, "Serial Number")!!.toLong()
        val messageCode = getLine(lines, "Space Weather Message Code")!!
        val issueTime = parseTime(getLine(lines, "Issue Time")!!)!!
        val title = getLine(lines, "Watch")!!

        val potentialImpactsIndex = notification.message.indexOf("Potential Impacts: ")
        val potentialImpacts = if (potentialImpactsIndex != -1) {
            notification.message.substring(potentialImpactsIndex + "Potential Impacts: ".length)
        } else {
            null
        }

        return SpaceWeatherWatch(
            serialNumber,
            notification.productId,
            messageCode,
            issueTime,
            title,
            potentialImpacts,
            notification.message
        )
    }

    private fun parseAlert(notification: RawSpaceWeatherAlert): SpaceWeatherNotification {
        val lines = notification.message.split("\r\n").map { it.split(":").map { it.trim() } }

        val serialNumber = getLine(lines, "Serial Number")!!.toLong()
        val messageCode = getLine(lines, "Space Weather Message Code")!!
        val issueTime = parseTime(getLine(lines, "Issue Time")!!)!!
        val title = getLine(lines, "Alert", "Continued Alert")!!
        val continuationOf = getLine(lines, "Continuation of Serial Number")?.toLongOrNull()

        val potentialImpactsIndex = notification.message.indexOf("Potential Impacts: ")
        val potentialImpacts = if (potentialImpactsIndex != -1) {
            notification.message.substring(potentialImpactsIndex + "Potential Impacts: ".length)
        } else {
            null
        }

        return SpaceWeatherAlert(
            serialNumber,
            notification.productId,
            messageCode,
            issueTime,
            title,
            potentialImpacts,
            notification.message,
            continuationOf
        )
    }

    private fun parseSummary(notification: RawSpaceWeatherAlert): SpaceWeatherNotification {
        val lines = notification.message.split("\r\n").map { it.split(":").map { it.trim() } }

        val serialNumber = getLine(lines, "Serial Number")!!.toLong()
        val messageCode = getLine(lines, "Space Weather Message Code")!!
        val issueTime = parseTime(getLine(lines, "Issue Time")!!)!!
        val title = getLine(lines, "Summary")!!

        val potentialImpactsIndex = notification.message.indexOf("Potential Impacts: ")
        val potentialImpacts = if (potentialImpactsIndex != -1) {
            notification.message.substring(potentialImpactsIndex + "Potential Impacts: ".length)
        } else {
            null
        }

        return SpaceWeatherSummary(
            serialNumber,
            notification.productId,
            messageCode,
            issueTime,
            title,
            potentialImpacts,
            notification.message
        )
    }

    private fun parseTime(time: String): ZonedDateTime? {
        // Parse the UTC time in the format of 2023 Apr 24 0000 UTC
        val parts = time.split(" ")
        if (parts.size < 4) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parseMonth(parts[1]) ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        val hour = parts[3].substring(0, 2).toIntOrNull() ?: return null
        val minute = parts[3].substring(2, 4).toIntOrNull() ?: return null
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC)
    }

    private fun parseMonth(month: String): Int? {
        return when (month.lowercase()) {
            "jan" -> 1
            "feb" -> 2
            "mar" -> 3
            "apr" -> 4
            "may" -> 5
            "jun" -> 6
            "jul" -> 7
            "aug" -> 8
            "sep" -> 9
            "oct" -> 10
            "nov" -> 11
            "dec" -> 12
            else -> null
        }
    }

    private fun getLine(lines: List<List<String>>, vararg keys: String): String? {
        val lowerKeys = keys.map { it.lowercase() }
        return lines.firstOrNull { lowerKeys.contains(it.firstOrNull()?.lowercase()) }?.lastOrNull()
    }

}