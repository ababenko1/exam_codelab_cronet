package com.google.codelabs.cronet

import android.util.Log
import com.google.codelabs.cronet.CronetCodelabConstants.LOGGER_TAG
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.time.Duration
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CronetImageDownloader(val engine: CronetEngine): ImageDownloader {

    private val executor = Executors.newSingleThreadExecutor()

    override suspend fun downloadImage(urlString: String): ImageDownloaderResult {
       val startNanoTime = System.nanoTime()
       return suspendCoroutine { continuation ->
           val request = engine.newUrlRequestBuilder(urlString, object: ReadToMemoryCronetCallback() {
               override fun onSucceeded(
                   request: UrlRequest,
                   info: UrlResponseInfo,
                   bodyBytes: ByteArray,
               ) {
                   continuation.resume(
                       ImageDownloaderResult(
                       successful = true,
                       blob = bodyBytes,
                       latency = Duration.ofNanos(System.nanoTime() - startNanoTime),
                       wasCached = info.wasCached(),
                       downloaderRef = this@CronetImageDownloader)
                   )
               }

               override fun onFailed(
                   request: UrlRequest,
                   info: UrlResponseInfo,
                   error: CronetException,
               ) {
                   Log.w(LOGGER_TAG, "Cronet download failed!", error)
                   continuation.resume(
                       ImageDownloaderResult(
                           successful = false,
                           blob = ByteArray(0),
                           latency = Duration.ZERO,
                           wasCached = info.wasCached(),
                           downloaderRef = this@CronetImageDownloader)
                   )
               }
           }, executor)
           request.build().start()
       }
    }
}