package com.justai.junction

import com.justai.aimybox.Aimybox
import com.justai.junction.aimybox.createAimybox
import com.justai.junction.viewmodel.viewModelModule
import me.lambdatamer.kandroid.KApplication
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

class AimybirdsApp : KApplication() {

    lateinit var aimybox: Aimybox

    override val rootModule = Kodein.Module("Root") {
        import(viewModelModule)
        bind() from singleton { aimybox }
    }

    override fun onCreate() {
        super.onCreate()
        aimybox = createAimybox(this)
    }
}