package com.example.gpstest

import java.text.SimpleDateFormat
import java.util.*

class Common {

    // Staticで利用できるようにする
    companion object {
        fun getToday(): String {
            val date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
    }
}