package wen.jie.imagegallery.model

import wen.jie.imagegallery.services.ApiFactory
import wen.jie.imagegallery.utils.ProgressResponseBody

object DownloadImageService {
    fun create(onAttachmentDownloadListener: ProgressResponseBody.OnAttachmentDownloadListener) : DownloadImageApi {
        return ApiFactory.createService(DownloadImageApi.BASE_URL, DownloadImageApi::class.java, progressListener = onAttachmentDownloadListener)
    }
}