package wen.jie.imagegallery.model

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface DownloadImageApi {
    companion object {
        var BASE_URL = "https://pixabay.com/"
    }

    @GET("get/{name}")
    @Streaming
    fun getImage (
        @Path("name") name: String
    ) : Observable<Response<ResponseBody>>
}