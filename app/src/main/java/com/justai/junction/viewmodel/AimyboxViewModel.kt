package com.justai.junction.viewmodel

import android.app.Application
import com.justai.aimybox.Aimybox
import me.lambdatamer.kandroid.viewmodel.KViewModel
import org.kodein.di.generic.instance

class AimyboxViewModel(a: Application) : KViewModel(a) {

    val aimybox by instance<Aimybox>()

    fun startTalking() {
        aimybox.sendRequest("")
    }

}