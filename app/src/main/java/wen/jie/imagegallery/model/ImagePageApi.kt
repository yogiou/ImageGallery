package wen.jie.imagegallery.model

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import wen.jie.imagegallery.Config

interface ImagePageApi {
    companion object {
        private const val PREFIX = "api/"
        var BASE_URL = Config.BASE_API_URL
    }

    @Headers("Content-Type: application/json")
    @GET(PREFIX)
    fun getImageByPage (
        @Query("key") key: String = "10961674-bf47eb00b05f514cdd08f6e11",
        @Query("page") page: String
    ) : Observable<ResponseData>
}