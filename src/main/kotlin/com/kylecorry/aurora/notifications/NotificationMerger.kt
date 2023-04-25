package com.kylecorry.aurora.notifications

internal object NotificationMerger {

    fun merge(notifications: List<SpaceWeatherNotification>): List<SpaceWeatherNotification> {
        val merged = mutableListOf<SpaceWeatherNotification>()
        notifications.sortedBy { it.issueTime }.forEach { notification ->
            var processed = false
            if (notification is SpaceWeatherWarning) {
                // Check if it cancels a previous warning
                if (notification.cancellationOf != null) {
                    merged.removeIf { it.serialNumber == notification.cancellationOf }
                    processed = true
                }

                // Check if it extends a previous warning
                if (notification.extensionOf != null) {
                    val previous = merged.find { it.serialNumber == notification.extensionOf }
                    merged.remove(previous)

                    if (previous is SpaceWeatherWarning) {
                        merged.add(notification.copy(originalSerialNumber = previous.originalSerialNumber))
                    } else {
                        merged.add(notification)
                    }
                    processed = true
                }
            }

            if (!processed) {
                merged.add(notification)
            }
        }
        return merged.sortedByDescending { it.issueTime }
    }

}