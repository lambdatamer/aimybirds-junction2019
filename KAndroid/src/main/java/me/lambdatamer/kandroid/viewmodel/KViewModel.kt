package me.lambdatamer.kandroid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import me.lambdatamer.kandroid.atom.Atom
import me.lambdatamer.kandroid.extensions.className
import me.lambdatamer.kandroid.extensions.name
import me.lambdatamer.kandroid.log.KLogger
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.subKodein
import org.kodein.di.android.x.closestKodein
import kotlin.coroutines.CoroutineContext

abstract class KViewModel(
    application: Application
) : AndroidViewModel(application), CoroutineScope, KodeinAware {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName(className)

    override val kodein: Kodein by closestKodein()

    val context = application

    protected val L = KLogger("${application.name}/$className")

    fun <T> MutableLiveData<Atom<T>>.launchRequest(block: suspend () -> T) =
        launchRequestInternal(this, block)

    @Suppress("ThrowableNotThrown")
    internal open fun <T> launchRequestInternal(
        liveData: MutableLiveData<Atom<T>>,
        block: suspend () -> T
    ) = launch {
        withContext(Dispatchers.Main) { liveData.value = Atom.Loading() }
        val result = try {
            Atom.Success(block())
        } catch (e: Throwable) {
            L.e(e)
            Atom.Error<T>(e)
        }
        liveData.postValue(result)
    }

    fun <T> ReceiveChannel<T>.toLiveData() = MutableLiveData<T>().also { liveData ->
        launch {
            consumeEach { item ->
                withContext(Dispatchers.Main) {
                    liveData.value = item
                }
            }
        }.invokeOnCompletion { cancel() }
    } as LiveData<T>

    inline fun subKodein(crossinline init: Kodein.MainBuilder.() -> Unit) =
        subKodein(closestKodein(), init = init)
}

