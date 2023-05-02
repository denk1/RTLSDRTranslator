package com.suvairin.rtlsdrtranslator.downloader

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.IOException
import java.security.AccessController.getContext


class AndroidDownloader(
    private val context: Context
): Downloader {
    private  val downloadManager = context.getSystemService(DownloadManager::class.java)
    override fun downloadFile(url: String): Long {
        if(isFileExists("playlist.xspf"))
            deleteFile("playlist.xspf")
        val request = DownloadManager.Request(url.toUri())
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI  )
            //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("playlist.xspf")
            .addRequestHeader("Authorization", "Bearer <token>")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "playlist.xspf")
        return downloadManager.enqueue(request)
    }

    private fun isFileExists(filename: String): Boolean {
        val path: String = Environment.getExternalStorageDirectory().absolutePath +
                "/" + Environment.DIRECTORY_DOWNLOADS +
                "/" + filename
        val localFile = File(path)
        return localFile.exists()
    }

    private fun deleteFile(filename: String) {
        val path: String = Environment.getExternalStorageDirectory().absolutePath +
                "/" + Environment.DIRECTORY_DOWNLOADS +
                "/" + filename
        val folder1 = File(path)

        try {
            if(folder1.delete())
                Log.d("0000", "deleted")
            else
                Log.d("0000", "Not deleted")
        } catch (ex: IOException) {
            Log.d("0000", "exception emitted")
        }

    }
}