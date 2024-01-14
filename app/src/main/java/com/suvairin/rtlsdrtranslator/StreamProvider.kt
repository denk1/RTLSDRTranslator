package com.suvairin.rtlsdrtranslator

import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamProvider(private val location: String ): MediaPlayer.OnPreparedListener  {

    private var mediaPlayer: MediaPlayer? = null
    private var prepared:Boolean? = false

    init {

    }


    private fun setMediaPlayer(  location:String ): MediaPlayer {
        isPrepared = false
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(location)
            setOnPreparedListener(this@StreamProvider)
            prepareAsync() // might take long! (for buffering, etc)
        }
    }

    fun stopStream() {
        mediaPlayer?.pause()
    }

    private fun startStream() {
        mediaPlayer?.start()
    }

    fun releaseStream() {
        mediaPlayer?.release()
        mediaPlayer = null

    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        startStream()
        isPrepared = true
    }
    val isPlaying:Boolean? get() {
        return mediaPlayer?.isPlaying
    }

    var isPrepared:Boolean? get() {
        return prepared
    } set(value) {
        prepared = value
    }

    fun play() {
        mediaPlayer = setMediaPlayer(location)
    }

}