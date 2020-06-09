package com.example.gpstest

import android.Manifest
import android.app.ActivityManager
import android.app.IntentService
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.SettingInjectorService
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import com.github.kittinunf.fuel.httpPost
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private var requestingLocationUpdates: Boolean = false

    private val REQUEST_CHECK_SETTINGS = 0x1
    private val REQUEST_BACKGROUND_SETTINGS = 0x11
    private val REQUEST_ALL = 0x12
    private var locationRequest: LocationRequest? = null
    private lateinit var startTime : String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 位置情報の初期設定や権限確認など
        createLocationRequest()

        val intent3 = Intent(this, GpsBackgroundService2::class.java)
        setBtnEnabled(false)

        // 開始ボタン
        btn_start.setOnClickListener {
            startService(intent3)
            setBtnEnabled(true)
            txtStartTime.setText("開始時刻：" + Common.getToday())
        }

        // 終了ボタン
        btn_stop.setOnClickListener {
            stopService(intent3)
            setBtnEnabled(false)
            txtEndTime.setText("終了時刻：" + Common.getToday())
        }
    }

    // ----------------------------------------
    // 位置情報リクエストを設定する
    // 位置情報の精度や更新頻度などを設定可能
    // ----------------------------------------
    private fun createLocationRequest() {

        this.locationRequest = LocationRequest.create()?.apply {
            // 更新間隔(アプリが現在地の更新情報を受信する頻度をミリ秒単位で設定)
            interval = 10000

            // 最短更新間隔(アプリが現在地の更新情報を処理できる最高頻度をミリ秒単位で設定。この例では5秒間隔。)
            fastestInterval = 5000

            // 優先度(精度を調整可能。大雑把なほうが省電力)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }

        // 位置情報に接続するために、位置情報リクエストを追加
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)

        // 現在の設定が満たされているかチェックする
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            requestingLocationUpdates = true
        }

        // 以下のチェックは、Android端末の設定→位置情報がONになっていない場合にONにする設定。（アプリレベルの許可は別）
        // エラーが発生した場合でResolvableApiExceptionが発生した場合は位置情報サービスを有効にするか確認する
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                    requestingLocationUpdates = true
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    text_view.text = "エラー"
                }
            }
        }

        // アプリに位置情報の使用を許可する
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // ok→BackGroundが許可されているかチェック
            val backgroundLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            // 許可されている
            if (backgroundLocationPermissionApproved) {
            }
            // 許可されていないのでバックグラウンド許可を求める
            else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_SETTINGS
                )
            }
        }
        // 許可されていない場合は許可を求める
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_ALL
            )
        }
    }

    fun isServiceWorking(clazz: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { clazz.name == it.service.className }
    }

    private fun setBtnEnabled(flgStart: Boolean) {
        if (flgStart) {
            btn_start.isEnabled = false
            btn_stop.isEnabled = true
        } else {
            btn_start.isEnabled = true
            btn_stop.isEnabled = false
        }
    }
}
