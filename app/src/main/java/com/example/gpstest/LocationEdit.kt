package com.example.gpstest

import android.util.Log
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

open class LocationEdit {
    private lateinit var realm:Realm

    // Realm登録
    public fun UpdateRealm(inID : Long, inTitle : String, inNote : String
                           , inStartDate : String, inEndDate : String) : Long {
        var id = inID ?:-1L
        realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val locdata: LocationData?
            when(id) {
                // 追加処理の場合はIDを採番
                -1L -> {
                    val maxId = realm.where<LocationData>().max("id")
                    id = ((maxId?.toLong() ?: 0L) + 1)
                    locdata = realm.createObject<LocationData>(id)
                }
                else -> {
                    locdata = realm.where<LocationData>().equalTo("id",id).findFirst()
                }
            }
            Log.d("LocationEdit.kt", "StartDate=" + inStartDate)
            // 追加 or 更新
            if (locdata != null) {
                locdata.title=inTitle ?: ""
                locdata.note=inNote ?: ""
                locdata.startdate= inStartDate
                locdata.enddate=inEndDate
            }
        }

        return id
    }
}