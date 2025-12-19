package com.suvairin.rtlsdrtranslator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap.Title
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.suvairin.rtlsdrtranslator.model.Actions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PlayerService : Service(), LifecycleOwner {
    private var providersMap: MutableMap<String, Pair<StreamProvider?, Boolean>>? = mutableMapOf();

    private lateinit var wakeLock: PowerManager.WakeLock
    private val binder = LocalBinder()
    private val TAG = "MyService"
    private val mServiceLifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private lateinit var playbackAttributes: AudioAttributes
    private var audioFocusRequest: Int = 0

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): PlayerService = this@PlayerService

    }

    override fun onBind(intent: Intent?): IBinder? {
        log("bind method")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        log("Service created")

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        mServiceLifecycleDispatcher.onServicePreSuperOnStart()

        if (intent != null) {
            val action = intent.action
            val location = intent.extras!!.getString("location")
            val title = intent.extras!!.getString("title")
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> createMediaPlayer(location!!)
                Actions.STOP.name -> stopService()
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

    private fun startService(location:String?, title:String?) {

        Log.d("ENDLESS-SERVICE", "Starting the foreground service task")
        //Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()

        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }


    }

    private fun stopService() {
        log("Stopping the foreground service")
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
        setServiceState(this, ServiceState.STOPPED)
    }

    fun startStream(uri: String) {

        if (providersMap?.contains(uri) != true) {

            providersMap?.put(
                uri,
                Pair<StreamProvider?, Boolean>(StreamProvider(uri), true)
            )

        }
        providersMap?.get(uri)?.first?.play()
        val str = "the stream is working with id [$uri]"
        log(str)

    }

    fun isPrepared(uri: String):Boolean? {
        return providersMap?.get(uri)?.first?.isPrepared
    }


    fun stopStream(uri: String) {
        providersMap?.get(uri)?.first?.stopStream()
    }

    fun releaseStream(uri: String) {
        providersMap?.get(uri)?.first?.releaseStream()
    }

    //val isPlaying:Boolean? get() = streamProvider.isPlaying

    fun isPlaying(uri: String): Boolean? {
        return providersMap?.get(uri)?.first?.isPlaying
    }

    fun isPreparing(uri: String): Boolean? {
        return providersMap?.get(uri)?.first?.isPrepared
    }

    override fun getLifecycle() = mServiceLifecycleDispatcher.lifecycle

    fun setStatusFuncFail(uri: String, setStatus : () -> Unit) {
        providersMap?.get(uri)?.first?.setStatusFuncFail(setStatus)

    }

    fun setStatusFuncOk(uri: String, setStatus : () -> Unit) {
        providersMap?.get(uri)?.first?.setStatusFuncOk(setStatus)
    }

    fun setRestoreConnFunc( uri: String, resoreConnFunc:  (String) -> Unit) {
        providersMap?.get(uri)?.first?.setRestoreConnFunc(resoreConnFunc)
    }

    fun start() {
        mMediaPlayer?.start()
    }

    fun pause() {
        mMediaPlayer?.pause()
    }

    fun stop() {
        mMediaPlayer?.stop()
    }

    fun release() {
        mMediaPlayer?.release()
    }

    val duration:Int?
        get() = mMediaPlayer?.duration

    val currentPosition:Int?
        get() = mMediaPlayer?.currentPosition

    val isPlaying:Boolean?
        get() = mMediaPlayer?.isPlaying


    fun seekTo(position:Int) {
        mMediaPlayer?.seekTo(position)
    }

    fun createMediaPlayer(location:String) {
        startForeground(1, App.notification)
        mMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(location)
            prepare() // might take long! (for buffering, etc)
            if (checkAudioFocus(audioFocusRequest)) {
                start()
                //binding.playBtn.setImageResource(R.drawable.ic_baseline_pause_24)

            }

        }
    }

    private fun checkAudioFocus(audioFocusRequest: Int): Boolean {
        return audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    val mediaPlayer
        get() = mMediaPlayer
}