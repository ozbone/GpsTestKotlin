package com.example.gpstest

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.text.SimpleDateFormat
import java.util.*

class GpsBackgroundService2() : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    lateinit var settingsClient: SettingsClient

    private val GAE_URL :String = "<GAEのURL　※このURLにPOST送信するよ！>"
    private var locationRequest: LocationRequest?  = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    @TargetApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Android8.1以降では独自の通知チャネルを作成する必要があるらしいので
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("gps_service","GPS Service")
            } else {
                ""
            }

        val notificationBuilder = Notification.Builder(this,channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        // スレッドで開始する！
        Thread(
            Runnable {
                startGpsJob(intent)
                //stopForeground(true)
            }).start()

        startForeground(1, notification)

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    // -----------------------------------
    //  GPS処理開始
    // -----------------------------------
    private fun startGpsJob(intent: Intent?) {
        // 位置情報サービスクライアントを作成する
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // callbackの設定
        setCallBack()

        val dataString = intent?.dataString
        Log.d("GpsBackgroundService", "MSG: ${intent?.getStringExtra("MSG")}")

        this.locationRequest = LocationRequest.create()?.apply {
            // 更新間隔(アプリが現在地の更新情報を受信する頻度をミリ秒単位で設定)
            interval = 10000

            // 最短更新間隔(アプリが現在地の更新情報を処理できる最高頻度をミリ秒単位で設定。この例では5秒間隔。)
            fastestInterval = 5000

            // 優先度(精度を調整可能。大雑把なほうが省電力)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }
        val builder = this.locationRequest?.let {
            LocationSettingsRequest.Builder().addLocationRequest(
                it
            )
        }

        settingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder?.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        Log.d("GpsBackgroundService","location-interval="+this.locationRequest?.interval.toString())

        // ハンドラースタート
        startLocationUpdates()
    }

    private fun setCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    Log.d("GpsBackgroundService", "onHandleIntent()-kousin : "
                            + location?.longitude.toString() + "," // 経度
                            + location?.latitude.toString() + ","  // 緯度
                            + location?.altitude.toString() + ","  // 高度
                            + Common.getToday())
                    sendGPS(location)
                }
            }
        }
    }

    // ハンドラー
    private fun startLocationUpdates() {
        Log.d("GpsBackgroundService", "startLocationUpdates()-start")
        fusedLocationClient.requestLocationUpdates(this.locationRequest,
            locationCallback,
            this.mainLooper /* Looper */) // Looperを指定しないと一回で終わってしまうので注意。
        Log.d("GpsBackgroundService", "startLocationUpdates()-end")
    }

    private fun sendGPS(location : Location) {
        // ちゃんと取得できていればGAEにPOSTする。
        // PubSubのクライアントはGAE側で定義する
        if (location != null) {
            // Postで送信
            val response1 = GAE_URL.httpPost(
                listOf(
                    "longitude" to location?.longitude.toString(), // 経度
                    "latitude" to location?.latitude.toString(),   // 緯度
                    "altitude" to location?.altitude.toString(),   // 高度
                    "dt" to Common.getToday()
                )
            ).response { request, response, result ->
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GpsBackgroundService", "終了")
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}