package com.example.gpstest

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_location_list.*

class LocationList : AppCompatActivity() {
    private lateinit var realm: Realm
    private val GAE_URL :String = "<GAEのURL>/showmap"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_list)
        realm = Realm.getDefaultInstance()
        var locdatas = realm.where<LocationData>().findAll()
        locdatas = locdatas.sort("id", Sort.DESCENDING)
        listView.adapter = LocationAdapter(locdatas)

        // listViewがクリックlされたときは地図を表示するよ！
        listView.setOnItemClickListener { parent,view,position,id ->
            val locdata:LocationData = parent.getItemAtPosition(position) as LocationData

            // 地図表示
            var uri = Uri.parse(GAE_URL + "?id=" + locdata.id)
            var intent_map : Intent = Intent(Intent.ACTION_VIEW,uri)
            startActivity(intent_map)
        }
    }
}