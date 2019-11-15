package com.justai.junction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.justai.junction.databinding.ActivityMainBinding
import com.justai.junction.databinding.FragmentMainBinding
import com.justai.junction.databinding.FragmentRadarBinding
import com.justai.junction.viewmodel.RadarViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.lambdatamer.kandroid.components.KActivity
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.createLogger
import me.lambdatamer.kandroid.viewmodel.viewModel
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : KActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainFragment())
            .commit()
    }
}

class MainFragment : KFragment() {
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.text.setText(R.string.scenario_start)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(R.id.container, RadarFragment())
            .commit()
    }
}

class RadarFragment : KFragment() {
    companion object {
        private const val MAX_RADAR_DELAY = 2000L
        private const val MIN_RADAR_DELAY = 100L
        private const val RADAR_DELAY_STEP = 50L
        private const val DISTANCE_MIN = 0.0
        private const val DISTANCE_MAX = 10.0

        private val delays =
            (MIN_RADAR_DELAY..MAX_RADAR_DELAY step RADAR_DELAY_STEP).toList().reversed()

        private val L = createLogger()
    }

    private val viewModel by viewModel<RadarViewModel>()

    private lateinit var binding: FragmentRadarBinding

    private var currentDelay = MAX_RADAR_DELAY

    override fun onStart() {
        super.onStart()
        launchRadar()
        viewModel.startScan()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopScan()
        coroutineContext.cancelChildren()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRadarBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.distance.observeNonNull { distance ->
            val delay = getDelayByDistance(distance ?: DISTANCE_MAX)
            L.d("Distance: $distance, delay: $delay")
            currentDelay = delay
        }
    }

    private fun getDelayByDistance(distance: Double): Long {
        val distance = min(distance, DISTANCE_MAX)

        val relativePosition = (DISTANCE_MAX - distance).pow(2.0) / DISTANCE_MAX.pow(2.0)

        val index = (relativePosition * (delays.size - 1)).roundToInt()

        return delays[index]
    }

    private fun launchRadar() = launch {
        while (isActive) {
            binding.radar.background.state = intArrayOf(
                android.R.attr.state_pressed,
                android.R.attr.state_enabled
            )
            delay(50)
            binding.radar.background.state = intArrayOf()

            delay(currentDelay)
        }
    }
}