package com.justai.junction

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.justai.junction.api.Quest
import com.justai.junction.databinding.FragmentMainBinding
import com.justai.junction.viewmodel.ImageFragment
import com.justai.junction.viewmodel.QuestStage
import com.justai.junction.viewmodel.QuestViewModel
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.onRippleClick
import me.lambdatamer.kandroid.extensions.withArguments
import me.lambdatamer.kandroid.viewmodel.viewModel

class MainFragment : KFragment() {

    private lateinit var binding: FragmentMainBinding

    private val questVM by viewModel<QuestViewModel> { kActivity }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        questVM.loadQuests()
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

        questVM.questList.observe {
            (activity as MainActivity).setProgressVisibility(false)
        }

        binding.button.onRippleClick {
            questVM.nextQuestOrStage()
        }

        questVM.currentQuest.observeNonNull { quest ->
            binding.container.removeAllViews()


            binding.textView.text = quest.mission
            binding.mainBackground.setBackgroundColor(Color.parseColor(quest.backgroundColor))
        }

        questVM.questStage.observeNonNull { stage ->
            val quest = requireNotNull(questVM.currentQuest.value)

            when (stage) {
                QuestStage.BRIEFING -> briefing(quest)
                QuestStage.SEARCHING -> searching(quest)
                QuestStage.TALKING -> talking(quest)
            }
        }
    }

    private fun briefing(quest: Quest) {
        binding.button.isVisible = true
        binding.textView.text = quest.briefing

        showImage(quest.imageUrl)
    }

    private fun searching(quest: Quest) {
        binding.button.isVisible = false
        binding.textView.text = quest.mission

        binding.container.removeAllViews()

        childFragmentManager.beginTransaction().replace(
            R.id.container,
            RadarFragment().withArguments(
                RadarFragment.Arguments(quest.beaconId, quest.imageUrl)
            )
        ).commit()
    }

    private fun talking(quest: Quest) {
        binding.button.isVisible = false
        binding.textView.text = null

        //TODO
    }

    private fun showImage(url: String) {
        childFragmentManager.beginTransaction().replace(
            R.id.container,
            ImageFragment().withArguments(
                ImageFragment.Arguments(url)
            )
        ).commit()
    }
}