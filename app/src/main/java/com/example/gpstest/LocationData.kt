package com.example.gpstest

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class LocationData:RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var startdate: String = ""
    var enddate:  String = ""
    var title:String = ""
    var note:String = ""
}