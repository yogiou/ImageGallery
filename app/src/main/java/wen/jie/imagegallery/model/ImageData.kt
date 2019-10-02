package wen.jie.imagegallery.model

import com.squareup.moshi.Json
import java.io.Serializable

data class ImageData(
    @field:Json(name = "largeImageURL")
    val largeImageURL: String,
    @field:Json(name = "webformatHeight")
    val webformatHeight: Int,
    @field:Json(name = "webformatWidth")
    val webformatWidth: Int,
    @field:Json(name = "likes")
    val likes: Int,
    @field:Json(name = "imageWidth")
    val imageWidth: Int,
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "user_id")
    val user_id: String,
    @field:Json(name = "views")
    val views: Int,
    @field:Json(name = "comments")
    val comments: String,
    @field:Json(name = "pageURL")
    val pageURL: String,
    @field:Json(name = "imageHeight")
    val imageHeight: Int,
    @field:Json(name = "webformatURL")
    val webformatURL: String,
    @field:Json(name = "type")
    val type: String,
    @field:Json(name = "previewHeight")
    val previewHeight: Int,
    @field:Json(name = "tags")
    val tags: String,
    @field:Json(name = "downloads")
    val downloads: Int,
    @field:Json(name = "user")
    val user: String,
    @field:Json(name = "favorites")
    val favorites: Int,
    @field:Json(name = "imageSize")
    val imageSize: Int,
    @field:Json(name = "previewWidth")
    val previewWidth: Int,
    @field:Json(name = "userImageURL")
    val userImageURL: String,
    @field:Json(name = "previewURL")
    val previewURL: String
) : Serializable