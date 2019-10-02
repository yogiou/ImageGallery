package wen.jie.imagegallery.utils

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*


class ProgressResponseBody(
    private val responseBody: ResponseBody?,
    private val progressListener: OnAttachmentDownloadListener?
) : ResponseBody() {

    interface OnAttachmentDownloadListener {
        fun onAttachmentDownloadedSuccess()
        fun onAttachmentDownloadedError()
        fun onAttachmentDownloadedFinished()
        fun onAttachmentDownloadUpdate(percent: Int)
    }

    private lateinit var bufferedSource: BufferedSource

    override fun contentType(): MediaType? {
        return responseBody?.contentType()
    }

    override fun contentLength(): Long {
        return responseBody?.let { responseBody.contentLength() } ?: 0L
    }

    override fun source(): BufferedSource {
        responseBody?.let {
            bufferedSource = Okio.buffer(source(it.source()))
        }

        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            private var totalBytesRead = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                responseBody?.let {
                    val bytesRead: Long = super.read(sink, byteCount)

                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                    val percent = if (bytesRead == -1L) 100f else totalBytesRead.toFloat() / it.contentLength() * 100

                    progressListener?.onAttachmentDownloadUpdate(percent.toInt())

                    return bytesRead
                } ?: return super.read(sink, byteCount)
            }
        }
    }
}