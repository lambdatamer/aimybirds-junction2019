package com.justai.junction

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.justai.junction.databinding.FragmentRadarBinding
import com.justai.junction.viewmodel.QuestViewModel
import com.justai.junction.viewmodel.RadarViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.createLogger
import me.lambdatamer.kandroid.extensions.unpack
import me.lambdatamer.kandroid.viewmodel.viewModel
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class RadarFragment : KFragment() {
    companion object {
        private const val MAX_RADAR_DELAY = 2000L
        private const val MIN_RADAR_DELAY = 100L
        private const val RADAR_DELAY_STEP = 50L
        private const val DISTANCE_MIN = 0.0
        private const val DISTANCE_MAX = 10.0
        private const val DISTANCE_FOUND = 0.02

        private val delays =
            (MIN_RADAR_DELAY..MAX_RADAR_DELAY step RADAR_DELAY_STEP).toList().reversed()

        private val L = createLogger()
    }

    private val radarVM by viewModel<RadarViewModel>()
    private val questVM by viewModel<QuestViewModel> { kActivity }

    private lateinit var binding: FragmentRadarBinding

    private var currentDelay = MAX_RADAR_DELAY

    private lateinit var beeper: MediaPlayer

    private val arguments by lazy { requireArguments().unpack<Arguments>() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        radarVM.beaconId.value = arguments.beaconId
    }

    override fun onStart() {
        super.onStart()
        beeper = MediaPlayer.create(context, R.raw.beep)
        launchRadar()
        radarVM.startScan()
    }

    override fun onStop() {
        super.onStop()
        stopRadar()
    }

    private fun stopRadar() {
        beeper.release()
        radarVM.stopScan()
        coroutineContext.cancelChildren()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRadarBinding.inflate(inflater, container, false)

        Glide.with(requireContext())
            .load(arguments.imageUrl)
            .into(binding.image)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        radarVM.distance.observeNonNull { distance ->
            if (distance > DISTANCE_FOUND) {
                val delay = getDelayByDistance(distance ?: DISTANCE_MAX)
                binding.distance.text = distance.toString()
                L.d("Distance: $distance, delay: $delay")
                currentDelay = delay
            } else {
                stopRadar()
                questVM.nextQuestOrStage()
            }
        }
    }

    private fun getDelayByDistance(distance: Double): Long {
        val distance = min(distance, DISTANCE_MAX)

        val relativePosition = (DISTANCE_MAX - distance).pow(3.0) / DISTANCE_MAX.pow(3.0)

        val index = (relativePosition * (delays.size - 1)).roundToInt()

        return delays[index]
    }

    private fun launchRadar() = launch {
        while (isActive) {
            binding.radar.background.state = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_enabled
            )
            beeper.start()
            delay(50)
            binding.radar.background.state = intArrayOf()

            delay(currentDelay)
        }
    }

    @Parcelize
    data class Arguments(val beaconId: String, val imageUrl: String) : Parcelable
}