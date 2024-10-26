package com.walkingstepcounter.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Specify your desired format here
    return currentDate.format(formatter)
}

fun getCurrentDateTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Specify your desired format here
    return currentDateTime.format(formatter)
}


fun getCurrentDayOfWeek(): String {
    val currentDayOfWeek = LocalDate.now().dayOfWeek
    return currentDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
}


fun formatElapsedTime(timeInMillis: Long): String {
    val seconds = (timeInMillis / 1000) % 60
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
