package com.justai.junction

import com.justai.junction.viewmodel.viewModelModule
import me.lambdatamer.kandroid.KApplication
import org.kodein.di.Kodein

class JunkPileApp :  KApplication() {
    override val rootModule = Kodein.Module("Root") {
        import(viewModelModule)
    }
}