package com.google.codelabs.cronet

import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

private const val BYTE_BUFFER_CAPACITY_BYTES = 64 * 1024

abstract class ReadToMemoryCronetCallback: UrlRequest.Callback() {

    private val bytesReceived = ByteArrayOutputStream()
    private val receiveChannel = Channels.newChannel(bytesReceived)

    override fun onRedirectReceived(
        request: UrlRequest,
        info: UrlResponseInfo,
        newLocationUrl: String,
    ) {
        request.followRedirect()
    }

    override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
        request.read(ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY_BYTES))
    }

    override fun onReadCompleted(
        request: UrlRequest,
        info: UrlResponseInfo,
        byteBuffer: ByteBuffer,
    ) {
        byteBuffer.flip()
        receiveChannel.write(byteBuffer)
        byteBuffer.clear()
        request.read(byteBuffer)
    }

    final override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
       val bodyBytes = bytesReceived.toByteArray()
       onSucceeded(request, info, bodyBytes)
    }

    abstract fun onSucceeded(
        request: UrlRequest, info: UrlResponseInfo, bodyBytes: ByteArray)

    abstract override fun onFailed(request: UrlRequest, info: UrlResponseInfo, error: CronetException)
}