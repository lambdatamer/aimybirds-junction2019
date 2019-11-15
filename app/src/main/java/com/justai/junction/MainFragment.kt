package com.justai.junction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.justai.junction.databinding.FragmentMainBinding
import com.justai.junction.viewmodel.QuestViewModel
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.onRippleClick
import me.lambdatamer.kandroid.extensions.withArguments
import me.lambdatamer.kandroid.viewmodel.viewModel

class MainFragment : KFragment() {

    private lateinit var binding: FragmentMainBinding

    private val questVM by viewModel<QuestViewModel>()

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

        binding.text.setText(R.string.lorem)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questVM.questList.observe {
            (activity as MainActivity).setProgressVisibility(false)
        }

        binding.button.onRippleClick {
            questVM.nextQuest()
        }

        questVM.currentQuest.observeNonNull { quest ->
            binding.container.removeAllViews()
            childFragmentManager.beginTransaction().replace(
                R.id.container,
                RadarFragment().withArguments(
                    RadarFragment.Arguments(quest.beaconId, quest.imageUrl)
                )
            ).commit()

            binding.text.text = quest.mission
            binding.button.isVisible = false
        }
    }

    private fun setImage(url: String) {
        binding.container.run {
            removeAllViews()
            val imageView = ImageView(context)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            addView(imageView)
            Glide.with(requireContext())
                .load(url)
                .into(imageView)
        }
    }
}