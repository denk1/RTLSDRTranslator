package com.suvairin.rtlsdrtranslator.downloader

interface Downloader {
    fun downloadFile(url: String): Long
}