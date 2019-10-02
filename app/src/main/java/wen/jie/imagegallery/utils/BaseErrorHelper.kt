package wen.jie.imagegallery.utils

import wen.jie.imagegallery.utils.RxEvent


class BaseErrorHelper {
    class BaseError(val error: Throwable) : RxEvent
    class DefaultError(val error: Throwable) : RxEvent
    class NoInternetConnectionError(val error: Throwable) : RxEvent
}