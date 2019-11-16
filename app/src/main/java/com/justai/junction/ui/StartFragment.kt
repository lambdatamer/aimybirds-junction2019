package com.justai.junction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.justai.junction.databinding.FragmentStartBinding
import me.lambdatamer.kandroid.components.KFragment
import me.lambdatamer.kandroid.extensions.onRippleClick

class StartFragment : KFragment() {

    lateinit var binding: FragmentStartBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)

        binding.newGameButton.onRippleClick {
            (kActivity as MainActivity).startNewGame()
        }

        return binding.root
    }
}