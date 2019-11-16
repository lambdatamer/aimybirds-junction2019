package com.justai.junction.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.justai.aimybox.api.DialogApi.Event.ResponseReceived
import com.justai.aimybox.model.reply.TextReply
import com.justai.aimybox.speechtotext.SpeechToText.Event.*
import com.justai.aimybox.texttospeech.TextToSpeech
import com.justai.junction.R
import com.justai.junction.api.Quest
import com.justai.junction.databinding.FragmentMainBinding
import com.justai.junction.viewmodel.AimyboxViewModel
import com.justai.junction.viewmodel.QuestStage
import com.justai.junction.viewmodel.QuestViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.createLogger
import me.lambdatamer.kandroid.extensions.onRippleClick
import me.lambdatamer.kandroid.extensions.withArguments
import me.lambdatamer.kandroid.viewmodel.viewModel

class MainFragment : KFragment() {

    companion object {
        private const val TIMER = 60000
        private const val FAIL_URL = "https://imgur.com/i0hZrlX"
    }

    private val L = createLogger()

    private lateinit var binding: FragmentMainBinding

    private val questVM by viewModel<QuestViewModel> { kActivity }
    private val aimyboxVM by viewModel<AimyboxViewModel> { kActivity }

    private var lastResponseIsQuestion = false

    private var timerJob: Job? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        questVM.loadQuests()
    }

    override fun onStop() {
        super.onStop()
        questVM.stopPlayer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questVM.questList.observeNonNull {
            (activity as MainActivity).setProgressVisibility(false)
        }

        binding.button.onRippleClick {
            questVM.nextQuestOrStage()
        }

        binding.aimyboxButton.onRippleClick {
            aimyboxVM.aimybox.toggleRecognition()
        }

        questVM.currentQuest.observeNonNull { (quest, _, _) ->
            binding.container.removeAllViews()

            binding.textView.text = quest.mission
            binding.mainBackground.setBackgroundColor(Color.parseColor(quest.backgroundColor))
        }

        questVM.questStage.observeNonNull { stage ->
            val (quest, currentIndex, total) = requireNotNull(questVM.currentQuest.value)

            when (stage) {
                QuestStage.BRIEFING -> briefing(quest)
                QuestStage.SEARCHING -> searching(quest)
                QuestStage.TALKING -> {
                    if (currentIndex + 1 < total) talking(quest)
                    else finish(quest)
                }
            }
        }

        observeRecording()
        observeResponse()
        observeTTS()
    }

    private fun briefing(quest: Quest) {
        binding.button.isVisible = true
        binding.textView.text = quest.briefing
        binding.textView.isVisible = true
        binding.birdTextView.isVisible = false
        binding.aimyboxButton.isVisible = false
        binding.timerText.isVisible = false

        showImage(quest.imageUrl)
    }

    private fun searching(quest: Quest) {
        binding.button.isVisible = false
        binding.textView.text = quest.mission
        binding.textView.isVisible = true
        binding.birdTextView.isVisible = false
        binding.aimyboxButton.isVisible = false
        binding.timerText.isVisible = true

        binding.container.removeAllViews()

        timerJob = launch {
            runTimer().collect { timeLeft ->
                val secondsLeft = timeLeft / 1000
                val msLeft = timeLeft % 1000
                binding.timerText.text = String.format("%02d:%03d", secondsLeft, msLeft)
            }
            fail()
        }

        childFragmentManager.beginTransaction().replace(
            R.id.container,
            RadarFragment().withArguments(
                RadarFragment.Arguments(quest.beaconId, quest.imageUrl)
            )
        ).commitAllowingStateLoss()
    }

    private fun talking(quest: Quest) {
        timerJob?.cancel()

        binding.button.isVisible = false
        binding.textView.text = null
        binding.textView.isVisible = false
        binding.timerText.isVisible = false

        binding.aimyboxButton.isVisible = true

        showImage(quest.imageUrl)

        aimyboxVM.startTalking()
    }

    private fun finish(quest: Quest) {
        binding.button.isVisible = true
        binding.button.text = getString(R.string.mission_completed)
        binding.button.onRippleClick {
            (activity as MainActivity).showStartMenu()
        }

        binding.textView.text = null
        binding.textView.isVisible = false
        binding.timerText.isVisible = false

        binding.aimyboxButton.isVisible = false

        showImage(quest.imageUrl)

        aimyboxVM.startTalking()
    }

    private fun fail() {
        binding.button.isVisible = true
        binding.button.text = getString(R.string.finish)
        binding.button.onRippleClick {
            (activity as MainActivity).showStartMenu()
        }

        binding.timerText.isVisible = false
        binding.mainBackground.setBackgroundColor(Color.parseColor("#00c853"))

        binding.textView.isVisible = false
        binding.birdTextView.text = "Mission failed!"
        binding.birdTextView.isVisible = true

        binding.aimyboxButton.isVisible = false

        childFragmentManager.beginTransaction().replace(
            R.id.container,
            ImageFragment().withArguments(ImageFragment.Arguments(resId = R.drawable.pig_fail))
        ).commit()
    }

    private fun observeResponse() {
        val channel = aimyboxVM.aimybox.dialogApiEvents.openSubscription()
        launch {
            channel.consumeEach { event ->
                when (event) {
                    is ResponseReceived -> {
                        L.d(event.response.replies)
                        val replyText = event.response.replies
                            .filterIsInstance<TextReply>()
                            .joinToString { it.text }
                        binding.birdTextView.isVisible = replyText.isNotBlank()
                        binding.birdTextView.text = replyText

                        lastResponseIsQuestion = event.response.question == true
                    }
                }
            }
        }.invokeOnCompletion {
            channel.cancel()
        }
    }

    private fun observeTTS() {
        val channel = aimyboxVM.aimybox.textToSpeechEvents.openSubscription()
        launch {
            channel.consumeEach { event ->
                when (event) {
                    is TextToSpeech.Event.SpeechSequenceCompleted -> {
                        if (!lastResponseIsQuestion) questVM.nextQuestOrStage()
                        lastResponseIsQuestion = true
                    }
                }
            }
        }.invokeOnCompletion {
            channel.cancel()
        }
    }

    private fun runTimer() = flow {
        for (i in TIMER downTo 0 step 10) {
            emit(i)
            delay(10)
        }
    }

    private fun observeRecording() {
        val channel = aimyboxVM.aimybox.speechToTextEvents.openSubscription()
        launch {
            channel.consumeEach { event ->
                when (event) {
                    is SoundVolumeRmsChanged -> {
                        binding.aimyboxButton.onRecordingVolumeChanged(event.rmsDb)
                    }
                    is RecognitionStarted -> {
                        binding.aimyboxButton.onRecordingStarted()
                    }
                    is RecognitionResult, RecognitionCancelled -> {
                        binding.aimyboxButton.onRecordingStopped()
                    }
                }
            }
        }.invokeOnCompletion {
            channel.cancel()
        }
    }

    private fun showImage(url: String) {
        childFragmentManager.beginTransaction().replace(
            R.id.container,
            ImageFragment().withArguments(ImageFragment.Arguments(url))
        ).commit()
    }
}