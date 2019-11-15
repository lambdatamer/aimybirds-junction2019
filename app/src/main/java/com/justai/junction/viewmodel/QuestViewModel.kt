package com.justai.junction.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.justai.junction.api.Quest
import com.justai.junction.api.QuestApi
import com.justai.junction.api.apiModule
import kotlinx.coroutines.launch
import me.lambdatamer.kandroid.viewmodel.KViewModel
import org.kodein.di.generic.instance

class QuestViewModel(application: Application) : KViewModel(application) {
    override val kodein by subKodein { import(apiModule) }

    private val questApi by instance<QuestApi>()

    val questList = MutableLiveData<List<Quest>>()

    val currentQuest = MutableLiveData<Quest>()

    fun loadQuests() = launch {
        val quests = questApi.getQuestList()
        questList.postValue(quests)
    }

    fun nextQuest(): Boolean {
        val questList = requireNotNull(questList.value)
        val currentIndex = currentQuest.value?.let(questList::indexOf) ?: -1
        val nextIndex = currentIndex + 1
        return if (nextIndex < questList.size) {
            currentQuest.postValue(questList[nextIndex])
            true
        } else {
            false
        }
    }

}