package com.suvairin.rtlsdrtranslator

import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamProvider(private val location: String ): MediaPlayer.OnPreparedListener,  MediaPlayer.OnErrorListener  {

    private var mediaPlayer: MediaPlayer? = null
    private var prepared:Boolean? = false
    private var statusFuncFail : (() -> Unit)? = null
    private var statusFuncOk : (() -> Unit)? = null
    private var resoreConnFunc:  ((String) -> Unit)? = null
    private var strUrl : String = ""

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
            strUrl = location
            setDataSource(location)
            setOnPreparedListener(this@StreamProvider)
            setOnErrorListener(this@StreamProvider)
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
        statusFuncOk?.invoke()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        log("an error has happened to the stream of $strUrl")
        mediaPlayer?.release()
        statusFuncFail?.invoke()
        resoreConnFunc?.invoke(strUrl)

        return true
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

    fun setStatusFuncFail(setStatus : () -> Unit) {
        statusFuncFail = setStatus
    }

    fun setStatusFuncOk(setStatus : () -> Unit) {
        statusFuncOk = setStatus
    }

    fun setRestoreConnFunc(resoreConn:  (String) -> Unit) {
        resoreConnFunc = resoreConn
    }
}