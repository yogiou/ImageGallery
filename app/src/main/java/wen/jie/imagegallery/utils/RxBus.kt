package wen.jie.imagegallery.utils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RxBus {
    private val rxBus = PublishSubject.create<RxEvent>()

    private object LazyLoader {
        internal val INSTANCE = RxBus()
    }

    companion object {
        fun getInstance(): RxBus {
            return LazyLoader.INSTANCE
        }
    }

    fun send(event: RxEvent) {
        rxBus.onNext(event)
    }

    fun toObservable(): Observable<RxEvent> {
        return rxBus
    }

    fun hasObservers(): Boolean {
        return rxBus.hasObservers()
    }

    fun getObservable(): Observable<RxEvent> {
        return rxBus.hide()
    }

    fun getObservable(clz: Class<*>): Observable<RxEvent> {
        return rxBus.ofType(clz).hide() as Observable<RxEvent>
    }
}