package com.example.gpstest

import android.app.Application
import io.realm.Realm

class GpsDB: Application() {
    override fun onCreate () {
        super.onCreate()

        // Realmを初期化する。
        Realm.init(this)
    }

}