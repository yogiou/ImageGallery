package wen.jie.imagegallery.viewmodel

import wen.jie.imagegallery.utils.BaseErrorHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import wen.jie.imagegallery.model.ImagePageApi
import wen.jie.imagegallery.model.ImageService
import wen.jie.imagegallery.model.ResponseData
import wen.jie.imagegallery.utils.RxBus
import wen.jie.imagegallery.utils.RxEvent

class ImageListFragmentViewModel {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val imagePageApi: ImagePageApi = ImageService.create()
    class DisplayOnShowLoading(val isVisible: Boolean) : RxEvent
    class DisplayOnShowImageList(val responseData: ResponseData) : RxEvent

    fun getImageLists(page: String) {
        imagePageApi.getImageByPage(page = page)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                RxBus.getInstance().send(DisplayOnShowLoading(true))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                RxBus.getInstance().send(DisplayOnShowLoading(false))
            }
            .subscribeBy(
                onNext = { responseData ->
                    RxBus.getInstance().send(DisplayOnShowImageList(responseData))
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