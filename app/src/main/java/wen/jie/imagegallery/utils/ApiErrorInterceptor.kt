package wen.jie.imagegallery.utils

import okhttp3.Interceptor
import okhttp3.Response

// handle Api error
object ApiErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response
        try {
            response = chain.proceed(chain.request())
        } catch (e: Throwable) {
            throw e
        }

        if (!response.isSuccessful) {
            val errorBody =
                try {
                    response.body()?.string()
                } catch (e: Throwable) {
                    throw e
                }
        }
        return response
    }
}