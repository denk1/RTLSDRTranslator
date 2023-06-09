package com.suvairin.rtlsdrtranslator

import android.Manifest
import android.app.DownloadManager
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.suvairin.rtlsdrtranslator.adapter.BroadcastAdapter
import com.suvairin.rtlsdrtranslator.databinding.ActivityMainBinding
import com.suvairin.rtlsdrtranslator.downloader.DownloadCompletedReceiver

class
MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var broadcastAdapter: BroadcastAdapter // Объект Adapter
    private lateinit var runnable: Runnable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        broadcastAdapter = BroadcastAdapter()
        setContentView(binding.root)
        val manager = LinearLayoutManager(this) // LayoutManager
        var downloadCompletedReceiver: DownloadCompletedReceiver = DownloadCompletedReceiver(broadcastAdapter)
        registerReceiver(downloadCompletedReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        requestPermissions()
        binding.recyclerView.layoutManager = manager // Назначение LayoutManager для RecyclerView
        binding.recyclerView.adapter = broadcastAdapter // Назначение адаптера для RecyclerView



    }

    private fun hasWriteExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    private fun hasNotificationPermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    private fun requestPermissions() {
        var permissionRequest = mutableListOf<String>()
        if(!hasWriteExternalStoragePermission()) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(!hasReadExternalStoragePermission()) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if(!hasNotificationPermission()) {
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if(permissionRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.isNotEmpty()) {
            for(i in grantResults.indices) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("0000", "${permissions[i]} granted.")
                    App.getInstance().initPlaylistService()
                }
            }
        }
    }
}

