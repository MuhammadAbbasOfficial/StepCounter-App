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