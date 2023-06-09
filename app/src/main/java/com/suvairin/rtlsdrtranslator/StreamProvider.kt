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

class StreamProvider(private val location: String ) {

    private var mediaPlayer: MediaPlayer? = null

    init {

    }


    private fun setMediaPlayer(  location:String ): MediaPlayer {
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(location)
            prepare() // might take long! (for buffering, etc)
            start()
        }
    }

    fun startMediaStream() = CoroutineScope(Dispatchers.Default).launch {
        mediaPlayer = setMediaPlayer(location)
    }

    fun stopStream() {
        mediaPlayer?.pause()
    }

    fun startStream() {
        mediaPlayer?.start()
    }

    fun releaseStream() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    val isPlaying:Boolean? get() { return mediaPlayer?.isPlaying }

}