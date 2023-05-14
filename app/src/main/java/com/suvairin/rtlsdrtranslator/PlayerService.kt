package com.suvairin.rtlsdrtranslator

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log


class PlayerService : Service() {
    private lateinit var streamProvider: StreamProvider
    private val binder = LocalBinder()
    private val TAG = "MyService"

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PlayerService = this@PlayerService

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")


        //streamProvider2 = StreamProvider("http://79.164.82.177:15888/soapy_stream2.mp3", App.getInstance().getBindings.textViewStream2, resources)
        //streamProvider3 = StreamProvider("http://79.164.82.177:15888/stream.mp3", App.getInstance().getBindings.textViewStream3, resources)
        //streamProvider4 = StreamProvider("http://79.164.82.177:15888/approaches.mp3", App.getInstance().getBindings.textViewStream4, resources)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        streamProvider = StreamProvider(intent?.data.toString())
        streamProvider.startMediaStream()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    val isPlaying:Boolean? get() = streamProvider.isPlaying
}