package com.suvairin.rtlsdrtranslator.model

import android.content.Context
import android.icu.text.CaseMap.Title
import android.net.Uri
import android.os.Environment
import com.suvairin.rtlsdrtranslator.AppConfig
import com.suvairin.rtlsdrtranslator.downloader.AndroidDownloader
import org.simpleframework.xml.*
import org.simpleframework.xml.core.Persister
import java.io.File

typealias BroadcastListener = (broadcasts: List<Broadcast>) -> Unit

@Root(strict = false, name = "playlist")
class Playlist {
    @field:Element(name="trackList")
    lateinit var trackList: TrackList
}

@Root(strict = false, name = "track")
class Track {
    @field:Element(name="location")
    lateinit var location: String

    @field:Element(name = "title")
    lateinit  var title: String
}

@Root(name = "trackList", strict = false)
class TrackList {
    @field:ElementList(inline = true)
    lateinit var tracks: List<Track>
}

class PlaylistService(
    private val context: Context
)
{
    private val downloader = AndroidDownloader(context)
    private var broadcasts = mutableListOf<Broadcast>() // all broadcasts
    private lateinit var fileName:String
    init {
        fileName = Environment.getExternalStorageDirectory().absolutePath +
                "/" + Environment.DIRECTORY_DOWNLOADS + "/playlist.xspf"

        downloader.downloadFile(AppConfig.PlayerListUrl.MEDIA_LIST_URL)

    }

    public fun getData() {
        val fileContent: String = readFileDirectlyAsText(fileName)
        val serializer: Serializer = Persister()
        val dataFetch = serializer.read(Playlist::class.java, fileContent)
        var tracks = dataFetch.trackList.tracks

        broadcasts = tracks.map {
            Broadcast(
                location = Uri.parse(it.location),
                title = it.title
            )
        }.toMutableList()
    }

    val getDataList
        get() = broadcasts


    fun readFileDirectlyAsText(fileName: String): String
            = File(fileName).readText(Charsets.UTF_8)
}

data class Broadcast (
    val location: Uri,
    val title: String
        )