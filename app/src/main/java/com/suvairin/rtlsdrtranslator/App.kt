package com.suvairin.rtlsdrtranslator

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.suvairin.rtlsdrtranslator.databinding.ActivityPlayerBinding
import com.suvairin.rtlsdrtranslator.model.PlaylistService

class App: Application() {
    private lateinit var playlistService : PlaylistService
    private  lateinit var binding: ActivityPlayerBinding
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

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

        fun getInstance(): App {
            return instance!!
        }

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}