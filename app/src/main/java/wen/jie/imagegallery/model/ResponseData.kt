package wen.jie.imagegallery.model

import com.squareup.moshi.Json

class ResponseData (
    @field:Json(name = "totalHits")
    val totalHits: Int,
    @field:Json(name = "hits")
    val hits: List<ImageData>
)