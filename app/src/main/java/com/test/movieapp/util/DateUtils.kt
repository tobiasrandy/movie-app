package com.test.movieapp.util

import java.text.SimpleDateFormat
import java.util.*

fun convertDate(dateString: String, inputFormat: String, outputFormat: String): String {
    return try {
        val inputFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
        val outputFormat = SimpleDateFormat(outputFormat, Locale.getDefault())

        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}