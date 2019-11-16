package com.justai.junction.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.justai.junction.api.Quest
import com.justai.junction.api.QuestApi
import com.justai.junction.api.apiModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.lambdatamer.kandroid.viewmodel.KViewModel
import org.kodein.di.generic.instance

class QuestViewModel(application: Application) : KViewModel(application) {
    override val kodein by subKodein { import(apiModule) }

    private val questApi by instance<QuestApi>()

    val questList = MutableLiveData<List<Quest>>()

    val currentQuest = MutableLiveData<Quest>()

    val questStage = MutableLiveData<QuestStage>()

    fun loadQuests() = launch {
        val quests = questApi.getQuestList()
        withContext(Dispatchers.Main) {
            questList.value = quests
            nextQuestOrStage()
        }
    }

    fun nextQuestOrStage() = when (questStage.value) {
        null, QuestStage.TALKING -> {
            val questList = requireNotNull(questList.value)
            val currentIndex = currentQuest.value?.let(questList::indexOf) ?: -1
            val nextIndex = currentIndex + 1
            if (nextIndex < questList.size) {
                currentQuest.value = questList[nextIndex]
                questStage.value = QuestStage.BRIEFING
                true
            } else {
                false
            }
        }
        QuestStage.SEARCHING -> {
            questStage.value = QuestStage.TALKING
            true
        }
        QuestStage.BRIEFING -> {
            questStage.value = QuestStage.SEARCHING
            true
        }
    }

}

enum class QuestStage { BRIEFING, SEARCHING, TALKING }