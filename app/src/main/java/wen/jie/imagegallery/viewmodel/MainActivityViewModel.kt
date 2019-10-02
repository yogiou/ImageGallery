package wen.jie.imagegallery.viewmodel

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import wen.jie.imagegallery.model.DownloadImageApi
import wen.jie.imagegallery.utils.BaseErrorHelper
import wen.jie.imagegallery.utils.RxBus
import wen.jie.imagegallery.utils.RxEvent
import java.io.Serializable


class MainActivityViewModel {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    data class ImageResponse (
        val responseBody: ResponseBody?,
        val name: String
    ) : Serializable

    class SaveFileRxEvent(val imageResponse: ImageResponse?) : RxEvent

    fun downloadImage(downloadImageApi: DownloadImageApi, name: String) {
        downloadImageApi.getImage(name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { response ->
                    RxBus.getInstance().send(SaveFileRxEvent(ImageResponse(response.body(), name)))
                },
                onError = { error ->
                    RxBus.getInstance().send(BaseErrorHelper.BaseError(error))
                }
            ).addTo(compositeDisposable)
    }

    fun onCleared() {
        compositeDisposable.clear()
    }
}