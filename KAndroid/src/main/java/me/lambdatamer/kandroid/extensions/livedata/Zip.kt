package me.lambdatamer.kandroid.extensions.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import me.lambdatamer.kandroid.extensions.filterNotNull

fun <T1 : Any?, T2 : Any?, R> LiveData<T1>.zip(with: LiveData<T2>, zipper: (T1?, T2?) -> R): LiveData<R> = MediatorLiveData<R>().also { mediator ->
    fun update(d1: T1? = this.value, d2: T2? = with.value) {
        if (d1 != null && d2 != null) mediator.value = zipper(d1, d2)
    }
    mediator.addSource(this) { update(d1 = it) }
    mediator.addSource(with) { update(d2 = it) }
}

fun <T1 : Any?, T2 : Any?, T3: Any?, R> zip(l1: LiveData<T1>, l2: LiveData<T2>, l3: LiveData<T3>, zipper: (T1?, T2?, T3?) -> R): LiveData<R> = MediatorLiveData<R>().also { mediator ->
    fun update(d1: T1? = l1.value, d2: T2? = l2.value, d3: T3? = l3.value) {
        if (d1 != null && d2 != null && d3 != null) mediator.value = zipper(d1, d2, d3)
    }
    mediator.addSource(l1) { update(d1 = it) }
    mediator.addSource(l2) { update(d2 = it) }
    mediator.addSource(l3) { update(d3 = it) }
}

fun <T1 : Any?, T2 : Any?, T3: Any?, T4: Any?, R> zip(l1: LiveData<T1>, l2: LiveData<T2>, l3: LiveData<T3>, l4: LiveData<T4>, zipper: (T1?, T2?, T3?, T4?) -> R): LiveData<R> = MediatorLiveData<R>().also { mediator ->
    fun update(d1: T1? = l1.value, d2: T2? = l2.value, d3: T3? = l3.value, d4: T4? = l4.value) {
        if (d1 != null && d2 != null && d3 != null && d4 != null) mediator.value = zipper(d1, d2, d3, d4)
    }
    mediator.addSource(l1) { update(d1 = it) }
    mediator.addSource(l2) { update(d2 = it) }
    mediator.addSource(l3) { update(d3 = it) }
    mediator.addSource(l4) { update(d4 = it) }
}

fun <T1, T2, R> LiveData<T1>.zipNotNull(with: LiveData<T2>, zipper: (T1?, T2?) -> R): LiveData<R> =
    zip(with, zipper).filterNotNull()

fun <T1 : Any?, T2 : Any?, T3: Any?, R> zipNotNull(l1: LiveData<T1>, l2: LiveData<T2>, l3: LiveData<T3>, zipper: (T1?, T2?, T3?) -> R): LiveData<R> =
    zip(l1, l2, l3, zipper).filterNotNull()

fun <T1 : Any?, T2 : Any?, T3: Any?, T4: Any?, R> zipNotNull(l1: LiveData<T1>, l2: LiveData<T2>, l3: LiveData<T3>, l4: LiveData<T4>, zipper: (T1?, T2?, T3?, T4?) -> R): LiveData<R> =
    zip(l1, l2, l3, l4, zipper).filterNotNull()