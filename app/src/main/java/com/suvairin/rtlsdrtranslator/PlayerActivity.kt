package com.suvairin.rtlsdrtranslator

import android.app.Notification
import android.app.Notification.Action
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.SyncStateContract.Constants
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.suvairin.rtlsdrtranslator.databinding.ActivityPlayerBinding
import com.suvairin.rtlsdrtranslator.model.Actions
import java.text.SimpleDateFormat
import java.util.*


class PlayerActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityPlayerBinding
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
    private var timePos : SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var duration: kotlin.time.Duration? = null
    private var mediaPlayer: MediaPlayer? = null

    var mService: PlayerService? = null
    var mBound = false


    private var curr_pos: Int = 0
    private lateinit  var audioManager: AudioManager
    private lateinit var playbackAttributes: AudioAttributes

    private var data: Uri? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        App.getInstance().getBindings = binding
        // AudioManager initialization
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        playbackAttributes = AudioAttributes.Builder().
                             setUsage(AudioAttributes.USAGE_GAME).
                             setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).
                             build()
        val focusRequest: AudioFocusRequest  = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(playbackAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
                {
                    mediaPlayer?.start()
                    mediaPlayer?.seekTo(curr_pos)

                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
                {
                    mediaPlayer?.pause()
                    curr_pos = mediaPlayer?.currentPosition!!

                } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS)
                {

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
            if(checkAudioFocus(audioFocusRequest)) {
                start()
                binding.playBtn.setImageResource(R.drawable.ic_baseline_pause_24)

            }
        }
        val timeZone:SimpleTimeZone = SimpleTimeZone(0, "UTC")
        timePos.timeZone = timeZone
        binding.endPos.text = timePos.format(mediaPlayer?.duration)
        binding.seekbar.progress = 0
        binding.seekbar.max = mediaPlayer!!.duration

        binding.playBtn.setOnClickListener {
            if(!mediaPlayer!!.isPlaying) {
                if(checkAudioFocus(audioFocusRequest))
                    mediaPlayer?.start()
                binding.playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPlayer?.pause()
                binding.playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)

            }
        }


        binding.seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
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

        runnableStream1 = Runnable {
            when(mService?.isPlaying1) {
                true -> binding.textViewStream1.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_green_back, null)
                false -> binding.textViewStream1.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
                else -> binding.textViewStream1.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
            }
            handlerStream1.postDelayed(runnableStream1, 1000)
        }

        runnableStream2 = Runnable {
            when(mService?.isPlaying2) {
                true -> binding.textViewStream2.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_green_back, null)
                false -> binding.textViewStream2.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
                else -> binding.textViewStream2.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
            }
            handlerStream2.postDelayed(runnableStream2, 1000)
        }

        runnableStream3 = Runnable {
            when(mService?.isPlaying3) {
                true -> binding.textViewStream3.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_green_back, null)
                false -> binding.textViewStream3.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
                else -> binding.textViewStream3.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
            }
            handlerStream3.postDelayed(runnableStream3, 1000)
        }

        runnableStream4 = Runnable {
            when(mService?.isPlaying4) {
                true -> binding.textViewStream4.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_green_back, null)
                false -> binding.textViewStream4.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
                else -> binding.textViewStream4.background = ResourcesCompat.getDrawable(this.resources, R.drawable.back_red_drawable, null)
            }
            handlerStream4.postDelayed(runnableStream4, 1000)
        }

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
            //startMediaStream1()
            if(checkAudioFocus(audioFocusRequest)) {
                val intent = Intent(this@PlayerActivity, PlayerService::class.java)
                intent.action = Actions.START.name
                intent.data = Uri.parse("http://79.164.82.177:15888/stream.mp3")
                intent.putExtra("id", 1)
                startForegroundService(intent)
                if(!mBound)
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        }

        binding.turnOnStream2.setOnClickListener {
            if(checkAudioFocus(audioFocusRequest)) {
                val intent = Intent(this@PlayerActivity, PlayerService::class.java)
                intent.action = Actions.START.name
                intent.data = Uri.parse("http://79.164.82.177:15888/soapy_stream2.mp3")
                intent.putExtra("id", 2)
                startForegroundService(intent)
                if(!mBound)
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        }

        binding.turnOnStream3.setOnClickListener {
            if(checkAudioFocus(audioFocusRequest)) {
                val intent = Intent(this@PlayerActivity, PlayerService::class.java)
                intent.action = Actions.START.name
                intent.data = Uri.parse("http://79.164.82.177:15888/orange_pi.mp3")
                intent.putExtra("id", 3)
                startForegroundService(intent)
                if(!mBound)
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        }

        binding.turnOnStream4.setOnClickListener {
            if(checkAudioFocus(audioFocusRequest)) {
                val intent = Intent(this@PlayerActivity, PlayerService::class.java)
                intent.action = Actions.START.name
                intent.data = Uri.parse("http://79.164.82.177:15888/approaches.mp3")
                intent.putExtra("id", 4)
                startForegroundService(intent)
                if(!mBound)
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        }



    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            mService = binder.getService()
            if (mService != null) {
                handlerStream1.postDelayed(runnableStream1, 1000)
                handlerStream2.postDelayed(runnableStream2, 1000)
                handlerStream3.postDelayed(runnableStream3, 1000)
                handlerStream4.postDelayed(runnableStream4, 1000)
            }
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun checkAudioFocus(audioFocusRequest: Int) : Boolean {
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
        if(mBound)
            unbindService(connection)
        mBound = false
    }
}