package wen.jie.imagegallery.services

import android.util.Log
import wen.jie.imagegallery.utils.ApiErrorInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.*
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import wen.jie.imagegallery.Config
import wen.jie.imagegallery.utils.ProgressResponseBody
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

interface IApiClient {
    var mCookieManager: CookieManager?
    var mCertificatePinner: CertificatePinner?
    var mClient: OkHttpClient

    fun createHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder().build()
            chain.proceed(request)
        }
    }

    fun createMoshi(): Moshi {
        // Create Moshi instance and add support for Kotlin annotation and Datetime conversion
        return Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()
    }

    fun <T> createService(baseUrl: String, apiClass: Class<T>, oOAuthOrNot: Boolean = true, progressListener: ProgressResponseBody.OnAttachmentDownloadListener? = null): T {
        val moshi = createMoshi()
        // Build the retrofit with the above objects,
        // and use RxJava2CallAdapter to wrap the response to Observable
        // and use ScalarsConverter to support raw application/json in send request

        mClient = createOkhttpClient(ApiFactory.mCookieManager, ApiFactory.mCertificatePinner, progressListener = progressListener)

        val builder = Retrofit.Builder()
            .client(mClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
        return builder.build().create(apiClass)
    }

    fun createOkhttpClient(cookieManager: CookieManager? = null, certificatePinner: CertificatePinner? = null, progressListener: ProgressResponseBody.OnAttachmentDownloadListener? = null): OkHttpClient {
        // logging interceptor
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> Log.v("OkHttp", message) })
        logging.level = HttpLoggingInterceptor.Level.BODY

        var okHttpBuilder = OkHttpClient.Builder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    progressListener?.let {
                        val originalResponse = chain.proceed(chain.request())
                        return originalResponse.newBuilder()
                            .body(ProgressResponseBody(originalResponse.body(), progressListener))
                            .build()
                    } ?: return chain.proceed(chain.request())
                }
            })
            .addInterceptor(logging)
            .addInterceptor(ApiErrorInterceptor) // api error interceptor
            .addInterceptor(createHeaderInterceptor())
            .connectTimeout(Config.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Config.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Config.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .followRedirects(true)

        if (cookieManager != null) {
            okHttpBuilder.cookieJar(JavaNetCookieJar(cookieManager))
        }

        if (certificatePinner != null) {
            okHttpBuilder.certificatePinner(certificatePinner)
        }

        return okHttpBuilder.build()
    }

    fun createCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .build()
    }

    fun createCookieManager(): CookieManager {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        return cookieManager
    }
}

object ApiFactory : IApiClient {
    override var mCookieManager: CookieManager? = null // createCookieManager()
    override var mCertificatePinner: CertificatePinner? = null // createCertificatePinner()
    override var mClient: OkHttpClient = createOkhttpClient(mCookieManager, mCertificatePinner)
}