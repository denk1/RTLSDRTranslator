package com.suvairin.rtlsdrtranslator

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import com.suvairin.rtlsdrtranslator.databinding.ActivityPlayerBinding
import com.suvairin.rtlsdrtranslator.model.PlaylistService

class App: Application() {
    private lateinit var playlistService : PlaylistService
    private  lateinit var binding: ActivityPlayerBinding

    init {
        instance = this
    }

    public override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

//            initPlaylistService()

    }

    var getBindings:ActivityPlayerBinding
        get()  = this.binding
        set(value) {
            this.binding = value
        }




    public fun initPlaylistService() {
        playlistService = PlaylistService(App.applicationContext())
    }




    private fun hasWriteExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(applicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(applicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasReadMediaAudioPermission() =
        ActivityCompat.checkSelfPermission(applicationContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED



    public fun readData() {
        playlistService.getData()
    }

    val getBroadcast
        get() = playlistService.getDataList

    companion object {
        private var instance: App? = null

        var notification: Notification? = null

        fun getInstance(): App {
            return instance!!
        }

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        public  val CHANNEL_ID_1:String = "channel1"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_1,
                "Running Notification",
                NotificationManager.IMPORTANCE_HIGH
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )

            notification = NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("RTLSDRTranslator")
                .setContentText("Elapsed time: 00:50").build()
        }
    }
}