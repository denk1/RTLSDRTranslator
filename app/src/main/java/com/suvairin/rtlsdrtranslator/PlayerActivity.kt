package com.suvairin.rtlsdrtranslator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.suvairin.rtlsdrtranslator.databinding.ActivityPlayerBinding
import com.suvairin.rtlsdrtranslator.model.Actions
import java.text.SimpleDateFormat
import java.util.*


import android.app.ActivityManager


object MyServiceUtils {

    fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

        manager?.let {
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }

        return false
    }
}


class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    lateinit var runnable: Runnable
    lateinit var runnableStream1: Runnable
    lateinit var runnableStream2: Runnable
    lateinit var runnableStream3: Runnable
    lateinit var runnableStream4: Runnable
    private var audioFocusRequest: Int = 0
    private var handler = Handler(Looper.myLooper()!!)
    private var handlerStream1 = Handler(Looper.myLooper()!!)
    private var handlerStream2 = Handler(Looper.myLooper()!!)
    private var handlerStream3 = Handler(Looper.myLooper()!!)
    private var handlerStream4 = Handler(Looper.myLooper()!!)
    private var timePos: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var duration: kotlin.time.Duration? = null
    private var mediaPlayer: MediaPlayer? = null
    private val str_stream1:String = ""
    private val str_stream2:String = ""
    private val str_stream3:String = ""
    private val str_stream4:String = ""

    val streamMap = mapOf(str_stream1 to false,
                          str_stream2 to false,
                          str_stream3 to false,
                          str_stream4 to false).toMutableMap()

    var mService: PlayerService? = null
    var mBound = false


    private var curr_pos: Int = 0
    private lateinit var audioManager: AudioManager
    private lateinit var playbackAttributes: AudioAttributes

    private var data: Uri? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        App.getInstance().getBindings = binding
        // AudioManager initialization
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        playbackAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        val focusRequest: AudioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        mediaPlayer?.start()
                        mediaPlayer?.seekTo(curr_pos)

                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        mediaPlayer?.pause()
                        curr_pos = mediaPlayer?.currentPosition!!

                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

                    }

                }
                .build()

        audioFocusRequest = audioManager.requestAudioFocus(focusRequest)
        // the end of AudioManager initialization
        setContentView(binding.root)
        val intent = intent
        val title = intent.extras!!.getString("title")
        val location = intent.extras!!.getString("location")
        binding.musicTitle.text = title
        mediaPlayer = MediaPlayer().apply {
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
                binding.playBtn.setImageResource(R.drawable.ic_baseline_pause_24)

            }
        }
        val timeZone: SimpleTimeZone = SimpleTimeZone(0, "UTC")
        timePos.timeZone = timeZone
        binding.endPos.text = timePos.format(mediaPlayer?.duration)
        binding.seekbar.progress = 0
        binding.seekbar.max = mediaPlayer!!.duration

        binding.playBtn.setOnClickListener {
            if (!mediaPlayer!!.isPlaying) {
                if (checkAudioFocus(audioFocusRequest))
                    mediaPlayer?.start()
                binding.playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPlayer?.pause()
                binding.playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)

            }
        }


        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    mediaPlayer?.seekTo(pos)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })


        runnable = Runnable {
            binding.seekbar.progress = if (mediaPlayer != null) mediaPlayer!!.currentPosition else 0
            binding.curPos.text = timePos.format(binding.seekbar.progress)
            handler.postDelayed(runnable, 1000)
        }

        handler.postDelayed(runnable, 1000)

        mediaPlayer?.setOnCompletionListener {
            binding.playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            binding.seekbar.progress = 0
        }

        binding.backward.setOnClickListener {
            mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition - 1000)
        }

        binding.further.setOnClickListener {
            mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition + 1000)
        }

        binding.turnOnStream1.setOnClickListener {
            streamMap[str_stream1] = !streamMap[str_stream1]!!
            if(!MyServiceUtils.isMyServiceRunning(this, PlayerService::class.java))
                startStream()
            setMode(streamMap.keys.elementAt(0), binding.textViewStream1)

        }

        binding.turnOnStream2.setOnClickListener {
            streamMap[str_stream2] = !streamMap[str_stream2]!!
            if(!MyServiceUtils.isMyServiceRunning(this, PlayerService::class.java))
                startStream()
            setMode(streamMap.keys.elementAt(1), binding.textViewStream2)

        }

        binding.turnOnStream3.setOnClickListener {
            streamMap[str_stream3] = !streamMap[str_stream3]!!
            if(!MyServiceUtils.isMyServiceRunning(this, PlayerService::class.java))
                startStream()
            setMode(streamMap.keys.elementAt(2), binding.textViewStream3)

        }

        binding.turnOnStream4.setOnClickListener {
            streamMap[str_stream4] = !streamMap[str_stream4]!!
            if(!MyServiceUtils.isMyServiceRunning(this, PlayerService::class.java))
                startStream()
            setMode(streamMap.keys.elementAt(3), binding.textViewStream4)
        }

    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            mService = binder.getService()

            // /////////////////////////////////////////////////////////
            // initialization os indicators
            // /////////////////////////////////////////////////////////





            if(streamMap[streamMap.keys.elementAt(0)] == true) {
                mService?.startStream(streamMap.keys.elementAt(0))
                setStatusFunc(streamMap.keys.elementAt(0), binding.textViewStream1)
                setRestoreConnFunc(streamMap.keys.elementAt(0))
            }

            if(streamMap[streamMap.keys.elementAt(1)] == true) {
                mService?.startStream(streamMap.keys.elementAt(1))
                setStatusFunc(streamMap.keys.elementAt(1), binding.textViewStream2)
                setRestoreConnFunc(streamMap.keys.elementAt(1))
            }
            if(streamMap[streamMap.keys.elementAt(2)] == true) {
                mService?.startStream(streamMap.keys.elementAt(2))
                setStatusFunc(streamMap.keys.elementAt(2), binding.textViewStream3)
                setRestoreConnFunc(streamMap.keys.elementAt(2))
            }
            if(streamMap[streamMap.keys.elementAt(3)] == true) {
                mService?.startStream(streamMap.keys.elementAt(3))
                setStatusFunc(streamMap.keys.elementAt(3), binding.textViewStream4)
                setRestoreConnFunc(streamMap.keys.elementAt(3))
            }

            //handlerStream1.postDelayed(runnableStream1, 1000)
            //handlerStream2.postDelayed(runnableStream2, 1000)
            //handlerStream3.postDelayed(runnableStream3, 1000)
            //handlerStream4.postDelayed(runnableStream4, 1000)
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun checkAudioFocus(audioFocusRequest: Int): Boolean {
        return audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        //Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show()
        if (mBound)
            unbindService(connection)
        mBound = false
    }

    private fun startStream() {
        if (checkAudioFocus(audioFocusRequest)) {
            val intent = Intent(this@PlayerActivity, PlayerService::class.java)
            intent.action = Actions.START.name
            startForegroundService(intent)
            if (!mBound)
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private fun setStatus(url: String, textView: TextView?, colour: Int) {
        textView?.background = ResourcesCompat.getDrawable(this.resources, colour, null)
        if(colour == R.drawable.back_green_back)
            Log.i("streams", "the connection of $url has been established")
        else
            Log.i("streams", "the connection of $url has been lost")
    }

    private fun setMode(strUrl:String, textView: TextView) {
        if (streamMap[strUrl] == true) {

            mService?.startStream(strUrl)
            setStatusFunc(strUrl, textView)
            setRestoreConnFunc(strUrl)

        } else {
            when (mService?.isPlaying(strUrl)) {
                true -> {
                    mService?.releaseStream(strUrl)
                    setStatus(strUrl, textView, R.drawable.back_red_drawable)

                }
                false -> {
                    var test = 0;
                }

                else -> mService?.releaseStream(strUrl)
            }
        }
    }

    private fun setStatusFunc(strUrl:String, textView: TextView?) {
        val statusFuncFail: () -> Unit = {

             setStatus(strUrl, textView, R.drawable.back_red_drawable)
            streamMap[strUrl] == false
        }

        val statusFuncOk: () -> Unit = {
            setStatus(strUrl, textView, R.drawable.back_green_back)
            streamMap[strUrl] ==true
        }

        mService?.setStatusFuncFail(strUrl, statusFuncFail)
        mService?.setStatusFuncOk(strUrl, statusFuncOk)

    }

    private fun setRestoreConnFunc(url: String) {
        val resoreConnFunc:  (String) -> Unit = { strUrl: String ->

            if(streamMap[strUrl] == true) {
                mService?.startStream(strUrl)
                Log.i("streams", "$strUrl has been restored")
            }
        }
        mService?.setRestoreConnFunc(url, resoreConnFunc)
    }
}