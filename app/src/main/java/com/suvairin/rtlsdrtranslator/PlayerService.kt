package com.suvairin.rtlsdrtranslator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.suvairin.rtlsdrtranslator.model.Actions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PlayerService : Service(), LifecycleOwner {
    private var streamProvider1: StreamProvider? = null
    private var streamProvider2: StreamProvider? = null
    private var streamProvider3: StreamProvider? = null
    private var streamProvider4: StreamProvider? = null
    private lateinit var wakeLock: PowerManager.WakeLock
    private val binder = LocalBinder()
    private val TAG = "MyService"
    private var isServiceStarted = false
    private var isStream1Started = false
    private var isStream2Started = false
    private var isStream3Started = false
    private var isStream4Started = false


    private val mServiceLifecycleDispatcher = ServiceLifecycleDispatcher(this)

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PlayerService = this@PlayerService

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        log( "Service created")
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()

            startForeground(1, notification)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        mServiceLifecycleDispatcher.onServicePreSuperOnStart()

        val id: Int? = intent?.getIntExtra("id", -1)

        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService(id, intent?.data.toString())
                Actions.STOP.name -> stopService(id)
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    private fun startService(id:Int?, uri:String) {
        if (!isServiceStarted) {
            Log.d("ENDLESS-SERVICE", "Starting the foreground service task")
            //Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
            isServiceStarted = true
            setServiceState(this, ServiceState.STARTED)

            // we need this lock so our service gets not affected by Doze Mode
            wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                        acquire()
                    }
                }
        }
        when(id) {
            1 -> stream1(true, uri)
            2 -> stream2(true, uri)
            3 -> stream3(true, uri)
            4 -> stream4(true, uri)
            else -> log("This should never happen. No stream number in the received intent")
        }
    }

    private fun stopService(id:Int?) {
        log("Stopping the foreground service")
        when(id) {
            1 -> stream1(false, "")
            2 -> stream2(false, "")
            3 -> stream3(false, "")
            4 -> stream4(false, "")
            else -> log("This should never happen. No stream number in the received intent")
        }
        if( isStream1Started or isStream2Started or isStream3Started or isStream4Started)
            return
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun stream1(f:Boolean, uri:String) {
        // we're starting a loop in a coroutine

        if (isStream1Started and f)
            return
        isStream1Started = f
        GlobalScope.launch(Dispatchers.IO) {
            //while (isStream1Started) {
                launch(Dispatchers.IO) {
                    //pingFakeServer()
                    streamProvider1 = StreamProvider(uri)
                    streamProvider1?.startMediaStream()
                    val str:String = "Stream 1 is working with id"
                    log(str)
                }
                //delay(10000)
            //}
            log("End of the loop 1")
        }
    }

    private fun stream2(f:Boolean, uri:String) {
        // we're starting a loop in a coroutine
        if (isStream2Started and f)
            return
        isStream2Started = f
        GlobalScope.launch(Dispatchers.IO) {
            //while (isStream2Started) {
                launch(Dispatchers.IO) {
                    //pingFakeServer()
                    streamProvider2 = StreamProvider(uri)
                    streamProvider2?.startMediaStream()
                    val str:String = "Stream 2 is working with id"
                    log(str)
                }
                //delay(10000)
            //}
            log("End of the loop 2")
        }
    }
    private fun stream3(f:Boolean, uri:String) {
        // we're starting a loop in a coroutine
        if (isStream3Started and f)
            return
        isStream3Started = f
        GlobalScope.launch(Dispatchers.IO) {
            //while (isStream3Started) {
                launch(Dispatchers.IO) {
                    //pingFakeServer()
                    streamProvider3 = StreamProvider(uri)
                    streamProvider3?.startMediaStream()
                    val str:String = "Stream 3 is working"
                    log(str)
                }
                //delay(10000)
            //}
            log("End of the loop 3")
        }
    }
    private fun stream4(f:Boolean, uri:String) {
        // we're starting a loop in a coroutine
        if (isStream4Started and f)
            return
        isStream4Started = f
        GlobalScope.launch(Dispatchers.IO) {
            //while (isStream4Started) {
                launch(Dispatchers.IO) {
                    //pingFakeServer()
                    streamProvider4 = StreamProvider(uri)
                    streamProvider4?.startMediaStream()
                    val str:String = "Stream 4 is working"
                    log(str)
                }
                //delay(10000)
            //}
            log("End of the loop 4")
        }
    }
    override fun getLifecycle() = mServiceLifecycleDispatcher.lifecycle

    fun stopStream(id: Int?) {
        when(id) {
            1 -> {
                streamProvider1?.stopStream()
                isStream1Started = false
            }
            2 -> {
                streamProvider2?.stopStream()
                isStream2Started = false
            }
            3 -> {
                streamProvider3?.stopStream()
                isStream3Started = false
            }
            4 -> {
                streamProvider4?.stopStream()
                isStream4Started = false
            }
            else -> {
                log("This should never happen. No stream number in the received intent")
            }
        }
    }

    fun startStream(id: Int?) {
        when(id) {
            1 -> streamProvider1?.startStream()
            2 -> streamProvider2?.startStream()
            3 -> streamProvider3?.startStream()
            4 -> streamProvider4?.startStream()
            else -> {
                log("This should never happen. No stream number in the received intent")
            }
        }
    }

    fun releaseStream(id: Int?) {
        when(id) {
            1 -> streamProvider1?.releaseStream()
            2 -> streamProvider2?.releaseStream()
            3 -> streamProvider3?.releaseStream()
            4 -> streamProvider4?.releaseStream()
            else -> {
                log("This should never happen. No stream number in the received intent")
            }
        }
    }

    //val isPlaying:Boolean? get() = streamProvider.isPlaying
    val isPlaying1:Boolean? get() = streamProvider1?.isPlaying
    val isPlaying2:Boolean? get() = streamProvider2?.isPlaying
    val isPlaying3:Boolean? get() = streamProvider3?.isPlaying
    val isPlaying4:Boolean? get() = streamProvider4?.isPlaying
}