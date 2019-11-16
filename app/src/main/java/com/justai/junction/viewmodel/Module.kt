package com.justai.junction.viewmodel

import me.lambdatamer.kandroid.viewmodel.viewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind

val viewModelModule = Kodein.Module("ViewModel") {
    bind() from viewModel<RadarViewModel>()
    bind() from viewModel<QuestViewModel>()
    bind() from viewModel<AimyboxViewModel>()
}