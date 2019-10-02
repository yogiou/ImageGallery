package wen.jie.imagegallery.model

import wen.jie.imagegallery.services.ApiFactory

object ImageService {
    fun create() : ImagePageApi {
        return ApiFactory.createService(ImagePageApi.BASE_URL, ImagePageApi::class.java)
    }
}