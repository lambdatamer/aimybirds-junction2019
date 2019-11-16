package com.justai.junction.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.lifecycle.MutableLiveData
import com.justai.junction.R
import com.justai.junction.aimybox.BeaconIdSkill
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

    val currentQuest = MutableLiveData<Triple<Quest, Int, Int>>()

    val questStage = MutableLiveData<QuestStage>()

    private var player: MediaPlayer? = null

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
            val currentIndex = currentQuest.value?.second ?: -1

            val nextIndex = currentIndex + 1
            if (nextIndex < questList.size) {
                val quest = questList[nextIndex]

                BeaconIdSkill.currentId = quest.beaconId

                currentQuest.value = Triple(quest, nextIndex, questList.size)
                questStage.value = QuestStage.BRIEFING
                playSound(R.raw.briefing)
                true
            } else {
                false
            }
        }
        QuestStage.SEARCHING -> {
            questStage.value = QuestStage.TALKING
            playSound(R.raw.found)
            true
        }
        QuestStage.BRIEFING -> {
            questStage.value = QuestStage.SEARCHING
            playSound(R.raw.start_radar)
            true
        }
    }

    private fun playSound(@RawRes res: Int) {
        stopPlayer()
        player = MediaPlayer.create(context, res)
        player?.start()
    }

    fun stopPlayer() {
        player?.release()
    }
}

enum class QuestStage { BRIEFING, SEARCHING, TALKING }