package com.example.gpstest

import android.content.Context
import android.net.wifi.WifiManager
import java.text.SimpleDateFormat
import java.util.*

class Common {

    // Staticで利用できるようにする。
    companion object {
        fun getToday(): String {
            val date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
        fun getMac(context: Context): String {
            val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = manager.connectionInfo
            return info.macAddress.toUpperCase()
        }
    }


}